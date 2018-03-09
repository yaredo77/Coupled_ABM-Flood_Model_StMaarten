/**
 * Copyright (c) [2018] [Yared Abayneh Abebe]
 *
 * This file is part of Coupled_ABM-Flood_Model.
 * Coupled_ABM-Flood_Model is free software licensed under the CC BY-NC-SA 4.0
 * You are free to:
 *	 Share — copy and redistribute the material in any medium or format
 *   Adapt — remix, transform, and build upon the material
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *	 Attribution — You must give appropriate credit, provide a link to the license, 
 *				  and indicate if changes were made. You may do so in any reasonable 
 *				  manner, but not in any way that suggests the licensor endorses you 
 *				  or your use.
 *	 NonCommercial — You may not use the material for commercial purposes.
 *	 ShareAlike — If you remix, transform, or build upon the material, you must distribute 
 *				 your contributions under the same license as the original. 
 *   Full license description: https://creativecommons.org/licenses/by-nc-sa/4.0/
 */
 
package operationalStructure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import physicalStructure.Flood;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.util.FileUtils;
import stMaarten.GlobalVariables;
import stMaarten.StMaartenBuilder;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.processing.Operations;
import org.geotools.factory.Hints;
import org.geotools.gce.arcgrid.ArcGridReader;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.referencing.CRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.GeometryFactory;

import collectiveStructure.Household;
import mainDataCollection.CatchmentsWithSM;

/**
 * This class couples the agent-based model with a hydrodynamic model (1D-2D flood model). The process of coupling is
 * 1. based on development conditions in ABM, the curve numbers (CN) of each subcatchment is updated in StMaartenBuilder.subcatchmentCNList.
 * If the return period is greater than or equal to 5,
 * 2. a hydrology model input file (rainfall-runoff model) gets updated (update both the RR file and rain file).
 * 3. 1D-2D flood model runs and produces result file (for example, in case of MIKE Flood, the result file is .dfs2 file).
 * 4. the result file is post-processed. The ABM reads only a raster file in geotiff format. In case of .dfs2 result:
 * 		1. extract the maximum flood depth using MIKEZero Statistics toolbox (the result is another .dfs2 file).
 * 		2. convert the .dfs2 file to ASCII grid format using MIKEZero GIS toolbox.
 * 5. load the maximum flood depth and assess impact
 * 
 * 6. if structural FRM measure is implemented, update the network file if a new network or a hydraulic structure is implemented, 
 * or the cross-section file if there is a change in cross-section (widening of channel or river width). This process should take 
 * place before running the 1D-2D model in case of proactive measures (evaluated in the same tick by the number of flooded houses, 
 * perhaps, compared to previous years), or at the end after loading the maximum flood depth in case of reactive measures (evaluated 
 * in the next flood year).   
 * 
 * 
 * Assumptions: - The average lot size of every new house is 200m2 (0.02ha). 
 * 				- Drainage channels in MIKE11 have the same roughness coefficient every run (no blockage or special maintenance or cleaning is assumed).
 * 				- Houses/Buildings are represented by point feature (centroid of the house polygon) and are considered flooded if the point feature 
 * 				  intersects the flood maps.
 * 				- A house is considered to be flooded if the flood depth is greater than 5cm assuming that houses may have some level of floor elevation.
 * 				- Climate change impact is included neither in the design rainfall intensity nor in the surge simulation.
 * 
 * @param CN_per_house
 * 
 */
public class ABMFloodModelCouple {
	private static int check = 0;
	private static int flagMike11 = 0;
	private static int flagMike21 = 0;
	private static double tickLastStructuralMeasure = 0;
	private static List<Object> selectedCatchmentsSM = new ArrayList<Object>(); // saves the selected catchment where a structural measure (SM) is implemented
																				// since it is cleared every tick, it only has one object at a given time.
	
	@ScheduledMethod(start=1, interval=1, priority = 30)
	public static void runCouple() {
		if (Flood.getReturnPeriod() >= 5) {
			updatCN();
			readWriteRRFile();
			runMIKEFlood();
			runMIKEZeroStatistics();
			runMIKEZeroGIS();
			try {
				convertASCIIToRaster();
			} catch (IOException e) {
				e.printStackTrace();
			}
			assessImpact();  // in this case only count the number of flooded houses 
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// once the runCouple method runs, go to the FRMActionSituation class and decide where to implement and 
	// come back to this method to "actually" implement the measure
	@ScheduledMethod(start=1, interval=1, priority = 20)
	public static void FRMMeasureImplementation() {
		// instantiate a CatchmentsWithSM object (name is null) that holds the name of the catchment that structural measure will be implemented.
		CatchmentsWithSM catchmentWithSM = new CatchmentsWithSM("null");
		String selectedCatchment = FRMActionSituation.getSelectedFloodedCatchment();
		if (selectedCatchment != null) {
			if (selectedCatchment.equals("A") || selectedCatchment.equals("B") || selectedCatchment.equals("C") || selectedCatchment.equals("D") 
					|| selectedCatchment.equals("E") || selectedCatchment.equals("G") || selectedCatchment.equals("R")) {
				if (selectedCatchment.equals("G")) {
					updateMike21Bathymetry();
					flagMike21 = 1;
				} else {
					// this is to update network file for channel A. If flagMike11 is already equal to 2, there is no need to update flagMike11 since the 
					// updates in channel B includes the updates for channel A 
					if (selectedCatchment.equals("A")) {
						if (flagMike11 < 1)
							flagMike11 = 1; 
						else
							flagMike11 = 3; // both A and B
					}
					// in case of channel B, it is not only the cross-section, but the network and boundary files are also updated 
					// We need to also update the MIKE Flood connection points for the new layout of channels ba0 and ba1. Use the flag below
					// to updates the above mentioned files. 
					if (selectedCatchment.equals("B")) { 
						if (flagMike11 < 1)
							flagMike11 = 2; 
						else
							flagMike11 = 3; // both A and B
					}
					updateCrossectionFile(selectedCatchment);
				}
				// register the year when a measure is implemented. This is used for decision making in the FRMActionSituation.
				double tick = RunEnvironment.getInstance().getCurrentSchedule().getTickCount();
				setTickLastStructuralMeasure(tick);
				// overwrite the catchmentWithSM object. The catchmentName is equal to selectedCatchment
				catchmentWithSM = new CatchmentsWithSM(selectedCatchment);
			}
		} else {
			// create a catchmentWithSM object. If selectedCatchment is null, the catchmentName is "null" (a string form).
			catchmentWithSM = new CatchmentsWithSM("null");
			//System.out.println("No catchment satisfied the selection criteria!");
		}
		// add the catchmentWithSM object to the context (so that AggregateDataSource interface could access it) and to the selectedCatchmentsSM list.  
		// this data is used in the mainDataCollection.CatchmentsWithSMData class to collect an aggregate data of the catchments where SM is implemented
		StMaartenBuilder.context.add(catchmentWithSM);
		selectedCatchmentsSM.add(catchmentWithSM);
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// this method updates the CN values of subcatchments that are found in the repetitions_Subcatchment hashmap.
	// the method runs every tick irrespective of flood return period and updates StMaartenBuilder.subcatchmentCNList
	public static void updatCN() {
		// ASSUMPTION average increase in CN per house (average lot size of 0.02ha) is 0.1
		/** @param CN_per_house
		 * ASSUMPTION: average lot size of new houses is 0.02ha.
		 * This parameter defines the average increase in CN value per house (only based on lot size, it does not consider the slope).
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "CN per house" and is a double value.
		 */
		Parameters params = RunEnvironment.getInstance().getParameters();
		double CN_PER_HOUSE = (double)params.getValue("CN_per_house");

		for (Map.Entry<String, Integer> e : GlobalVariables.countSubcatchmentFrequency().entrySet()) {
			String search = e.getKey();
			for (int i = 0; i < StMaartenBuilder.getSubcatchmentNameListRR().size(); i++) {
				if (StMaartenBuilder.getSubcatchmentNameListRR().get(i).contains(search)) {
					double increment = e.getValue() * CN_PER_HOUSE; 
					double previousCN = StMaartenBuilder.getSubcatchmentCNList().get(i);
					double newCN = previousCN + increment; 
					// the maximum CN value is limited to 94
					if (newCN > 94.0) {
						newCN = 94.0;
					}
					StMaartenBuilder.getSubcatchmentCNList().set(i, newCN); 
					break;
				}
			}
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	// This method reads the original RR file, updates the CN based on StMaartenBuilder.subcatchmentCNList, rain time-series based on the
	// return period in the given tick, and finally writes in a new RR file. The updated RR file is used to run the MIKE Flood model.
	public static void readWriteRRFile() {
		// The name of the files to open.
		String rr_fileName = "data/hydrodynamic_data/MIKE11/SXM_RR_original.RR11";  // readfile
		String updatedrr_fileName = "data/hydrodynamic_data/MIKE11/SXM_RR_updated.RR11";  // writefile

		// This will reference one line at a time
		String line = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(rr_fileName);
			// FileWriter writes text files in the default encoding.
			FileWriter fileWriter = new FileWriter(updatedrr_fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			int totalNumberOfLines = 20677;  // The total number of lines in the MIKE11 RR file
			int changedCNLines = 2396; // The curve numbers of each catchment are written between lines 2396 and 14494
			int changedRainLines = 14541; // Time series rainfall data of catchments is written between lines 14541 and 20590
			int i = 0;
			for (int j = 0; j < totalNumberOfLines; j++) {   
				line = bufferedReader.readLine();
				if (j == changedCNLines && j < 14495) {  // update CN
					// This format is copied from the RR file (be careful with the number of spaces!)
					//int CN = (int)((double)StMaartenBuilder.subcatchmentCNList.get(i));  // cast Double to double and then to int
					bufferedWriter.write("         LossCurveNumber = " + (int)((double)StMaartenBuilder.getSubcatchmentCNList().get(i))); 
					changedCNLines += 46;  // The curve numbers of each catchment are written every 46 lines
					i++; // i runs upto the length of StMaartenBuilder.subcatchmentCNList
				} else if (j == changedRainLines && j < 20591) {  // update rain time series
					// This format is copied from the RR file (be careful with the number of spaces!)
					switch (Flood.getReturnPeriod()) {
					case 5:
						bufferedWriter.write("         Timeseries = |.\\RAIN FILES\\rain_T5_1h.dfs0|"); 
						break;
					case 10:
						bufferedWriter.write("         Timeseries = |.\\RAIN FILES\\rain_T10_1h.dfs0|"); 
						break;
					case 20:
						bufferedWriter.write("         Timeseries = |.\\RAIN FILES\\rain_T20_1h.dfs0|"); 
						break;
					case 50:
						bufferedWriter.write("         Timeseries = |.\\RAIN FILES\\rain_T50_1h.dfs0|"); 
						break;
					case 100:
						bufferedWriter.write("         Timeseries = |.\\RAIN FILES\\rain_T100_1h.dfs0|"); 
						break;
					}
					changedRainLines += 23;  // Rain time series of each catchment are written every 23 lines
				} else {
					bufferedWriter.write(line);  // write the rest here
				}
				bufferedWriter.newLine();
			}   

			// Always close files.
			bufferedReader.close(); 
			bufferedWriter.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + rr_fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + rr_fileName + "'");                  
			// Or we could just do this: 
			// ex.printStackTrace();
		}		
        // copy the updated RR files in case they are needed for further analysis
		String dateStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
		File updatedRR = new File("data/hydrodynamic_data/MIKE11/SXM_RR_updated.RR11");
        File copy_updatedRR = new File("data/hydrodynamic_data/MIKE21/Result/FloodMaps/updatedRR_tick" 
        								+ RunEnvironment.getInstance().getCurrentSchedule().getTickCount() 
        								+ "_" + dateStamp + ".RR1");
        try {
			FileUtils.copyFile(updatedRR, copy_updatedRR);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// This method runs MIKE Flood (1D-2D) Model. The result file is Result.dfs2
	public static void runMIKEFlood() {
		/******************************* 
		 * Before running MIKEFlood, make sure of using the right rr and xns file in MIKE11 sim11 file.
		 * Since the network and boundary files may also be changed from another method,  */
		// The name of the files to open.
		String simCopy_fileName = "data/hydrodynamic_data/MIKE11/SXM_Simulation_Copy.sim11";  // readfile
		String sim_fileName = "data/hydrodynamic_data/MIKE11/SXM_Simulation.sim11";  // writefile

		// This will reference one line at a time
		String line = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(simCopy_fileName);
			// FileWriter writes text files in the default encoding.
			FileWriter fileWriter = new FileWriter(sim_fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			int totalNumberOfLines = 89;  // The total number of lines in the sim11 file
			int changedNWKLines = 21; // The nwk11 input file is written at the 22nd line (starting from 1) 
			int changedXNSLines = 22; // The xns11 input file is written at the 23rd line (starting from 1) 
			int changedBNDLines = 23; // The bnd11 input file is written at the 24th line (starting from 1) 
			int changedRRLines = 24; // The RR11 input file is written at the 25th line (starting from 1) 
			for (int j = 0; j < totalNumberOfLines; j++) {   
				// read each line and if j is different from the above defined numbers, write what is read in this line.
				// otherwise, write the buffered writer text below the if statements.
				line = bufferedReader.readLine();
				if (j == changedNWKLines && check == 0) {  // first time, use SXM_Network.nwk11
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      nwk = |.\\SXM_Network.nwk11|"); 
				} else if (j == changedNWKLines && check > 0) {
					if (flagMike11 == 1) {  // change the nwk11 file if flagMike11 == 1 (if channel a is updated)
						// This format is copied from the sim11 file (be careful with the number of spaces!)
						bufferedWriter.write("      nwk = |.\\SXM_Network_updatedForChannel-A.nwk11|"); 
					} else if (flagMike11 == 2) {  // change the nwk11 file if flagMike11 == 2 (if channel b is updated)
						// This format is copied from the sim11 file (be careful with the number of spaces!)
						bufferedWriter.write("      nwk = |.\\SXM_Network_updatedForChannel-B.nwk11|"); 
					} else if (flagMike11 == 3) {  // change the nwk11 file if flagMike11 == 3 (if both channels a & b are updated)
						// This format is copied from the sim11 file (be careful with the number of spaces!)
						bufferedWriter.write("      nwk = |.\\SXM_Network_updatedForAllChannels.nwk11|"); 
					}
				} else if (j == changedXNSLines && check == 0) {  // first time, use SXM_XNS.xns11
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      xs = |.\\SXM_XNS.xns11|"); 
				} else if (j == changedXNSLines && check > 0) {  // once updated, use the widened xns11 file, i.e., SXM_XNS_FRM50.xns11
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      xs = |.\\SXM_XNS_FRM50.xns11|");
				} else if (j == changedBNDLines && check == 0) {  // first time, use SXM_Boundary.bnd11
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      bnd = |.\\SXM_Boundary.bnd11|");
				} else if (j == changedBNDLines && check > 0 && flagMike11 >= 2) {  // change the bnd11 file if flagMike11 >= 2 (if channel b is updated)
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      bnd = |.\\SXM_Boundary_updatedForChannel-B.bnd11|");
				} else if (j == changedRRLines) {  // update .RR11 file irrespective of the value of 'check'
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("      rr = |.\\SXM_RR_updated.RR11|");
				} else {
					bufferedWriter.write(line);  // write the rest here
				}
				bufferedWriter.newLine();
			}   

			// Always close files.
			bufferedReader.close(); 
			bufferedWriter.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + simCopy_fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + simCopy_fileName + "'");                  
		}
		
		/************************************* 
		 * at least for the very first MIKEFlood simulation, make sure to use the original bathymetry in the .m21 file */
		if ( flagMike21 == 0 && check == 0) { // evaluating both flags reduces the chance of executing the code inside the if-statement
			// The name of the files to open.
			String m21Copy_fileName = "data/hydrodynamic_data/MIKE21/St_Maarten_Full_Copy.m21";  // readfile
			String m21_fileName = "data/hydrodynamic_data/MIKE21/St_Maarten_Full.m21";  // writefile

			// This will reference one line at a time
			String lineM21 = null;
			try {
				// FileReader reads text files in the default encoding.
				FileReader fileReader = new FileReader(m21Copy_fileName);
				// FileWriter writes text files in the default encoding.
				FileWriter fileWriter = new FileWriter(m21_fileName);

				// Always wrap FileReader in BufferedReader.
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				// Always wrap FileWriter in BufferedWriter.
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

				int totalNumberOfLines = 571;  // The total number of lines in the sim11 file
				int changedBathymetryLines = 25; // The xns11 input file is written at the 23rd line (starting from 1) 
				for (int j = 0; j < totalNumberOfLines; j++) {   
					lineM21 = bufferedReader.readLine();
					if (j == changedBathymetryLines) {  // first time, use SXM_XNS.xns11
						// This format is copied from the sim11 file (be careful with the number of spaces!)
						bufferedWriter.write("               FILE_NAME = |.\\Bathymetry_St_Maarten_withroad30.dfs2|"); 
					} else {
						bufferedWriter.write(lineM21);  // write the rest here
					}
					bufferedWriter.newLine();
				}   

				// Always close files.
				bufferedReader.close(); 
				bufferedWriter.close();
			}
			catch(FileNotFoundException ex) {
				System.out.println("Unable to open file: '" + m21_fileName + "' or '" + m21Copy_fileName + "'");                
			}
			catch(IOException ex) {
				System.out.println("Error reading file: '" + m21_fileName + "' or '" + m21Copy_fileName + "'");                  
			}		
		}
		
		/************************************* 
		 * now run MIKEFlood */
		// ASSUMPTION drainage channels in MIKE11 have the same roughness coefficient every run (no blockage or special maintenance or cleaning is assumed)
		
		// MIKEFLOODLaunch.exe needs full .couple file path. Since RS batch simulation runs from the temp folder (C:/Users/user/AppData/Local/Temp/), providing the 
		// absolute path directly in the ProcessBuilder method only calls that specific file instead of the copied one in the temp folder. To avoid this issue, first
		// create a local variable (of type File) that holds the short file path, and then find and store the absolute path of the file in another local variable (of 
		// String type). Then use this String variable in the ProcessBuilder method. Wherever we copy the whole model file, we can guarantee now that the .couple file
		// has the correct absolute file path.
		File coupledFile = null;
		if (flagMike11 >= 2) { 
			coupledFile = new File("data/hydrodynamic_data/St_Maarten_Full_Coupled_ba0-ba1.couple");
		} else {
			coupledFile = new File("data/hydrodynamic_data/St_Maarten_Full_Coupled.couple");
		}
		String coupledFileString = coupledFile.getAbsolutePath();
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files (x86)\\DHI\\2016\\bin\\x64\\MIKEFLOODLaunch.exe", 
				coupledFileString, "-x"); // MIKEFLOODLaunch.exe needs full .couple file path
		try {
			Process p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// now, update check
		//check++; // TODO the position might change depending on proactive or reactive measures ... this case is proactive
	}

	/***********************************************************************************************
	***********************************************************************************************/
	// This method runs MIKE Zero Statistics toolbox. Maximum depth values are extracted from the 1D-2D result file.
	public static void runMIKEZeroStatistics() {
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files (x86)\\DHI\\2016\\bin\\x64\\ToolboxShell.exe", "-run", 
				"data\\hydrodynamic_data\\MIKE21\\extractMaxWaterDepth.mzt");
		try {
			Process p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	// This method runs MIKE Zero GIS toolbox. Maximum depth dfs2 file is converted to grid format.
	public static void runMIKEZeroGIS() {
		ProcessBuilder pb = new ProcessBuilder("C:\\Program Files (x86)\\DHI\\2016\\bin\\x64\\ToolboxShell.exe", "-run", 
				"data\\hydrodynamic_data\\MIKE21\\dfs2ToASCII.mzt");
		try {
			Process p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	// This method uses geotools GeoTiffWriter to convert maximum depth ascii grid format to geotiff.
	public static void convertASCIIToRaster() throws IOException {
		URL url = null;
		try {
			url = new File("data/hydrodynamic_data/MIKE21/Result/Result_Max_Depth0.asc").toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		
	    // if the CRS of the ASCII file is known, set the CRS manually using CRS Hints
		CoordinateReferenceSystem crs = null;
	    try {
	    	//String code = CRS.lookupIdentifier(crs, true);  // get EPSG of a projection
			crs = CRS.decode("EPSG:32620");  // EPSG:32620 corresponds to UTM20 North
		} catch (NoSuchAuthorityCodeException e) {
			e.printStackTrace();
		} catch (FactoryException e) {
			e.printStackTrace();
		}
	    
		// Alternatively, if we do not know the exact CRS and we would like to use the CRS of another file,
		// we need to first read the coverage of the file and then get the CRS from the coverage
		/*File file = new File("data/150mm_Depth_F.tif"); 
		AbstractGridFormat format = GridFormatFinder.findFormat(file);
	    GridCoverage2DReader tiffReader = format.getReader(file);
	    GridCoverage2D coverage = tiffReader.read(null);
	    CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();*/
	    
	    // create hints with default CRS key and pass the value as default CRS of the reader 
	    Hints hints = new Hints(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM, crs);
	    ArcGridReader ascReader = new ArcGridReader(url, hints);
        
	    if (ascReader != null) {
		    GridCoverage2D coverage = (GridCoverage2D) ascReader.read(null);
		    // to match the projection used in RS, transform the UTM20 to geographic (GCS_WGS_1984) 
		    CoordinateReferenceSystem targetCRS = null;
		    try {
				targetCRS = CRS.decode("EPSG:4326", true);  // EPSG:4326 corresponds to GCS_WGS_1984, and the boolean argument forces (long, lat) order
			} catch (NoSuchAuthorityCodeException e) {
				e.printStackTrace();
			} catch (FactoryException e) {
				e.printStackTrace();
			}
		    // resample the coverage using the target CRS
		    GridCoverage2D transformedCoverage = (GridCoverage2D)Operations.DEFAULT.resample(coverage, targetCRS);
		    
		    // output file and delete if it already exists (to avoid re-writing)
		    File floodMap_tiff = new File("data/hydrodynamic_data/MIKE21/Result/flood_map.tif");
	        if (floodMap_tiff.exists())
	            floodMap_tiff.delete();
	        //getting a format
	        GeoTiffFormat tiffFormat = new GeoTiffFormat();
	        //getting the write parameters
	        GeoTiffWriteParams wp = new GeoTiffWriteParams();
	        //setting compression to LZW
	        wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
	        wp.setCompressionType("LZW");
	        //setting the write parameters for this geotiff
            final ParameterValueGroup params = tiffFormat.getWriteParameters();
            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);
	        
	        GeoTiffWriter tiffWriter = (GeoTiffWriter)tiffFormat.getWriter(floodMap_tiff);
	        tiffWriter.write(transformedCoverage, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
	        //GeoTiffWriter tiffWriter = new GeoTiffWriter(floodMap_tiff);
	        //tiffWriter.write(ascCoverage, null);
	    	
	        // copy the flood maps in case they are needed for further analysis
	        String dateStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm").format(new Date());
	        File copy_floodMap_tiff = new File("data/hydrodynamic_data/MIKE21/Result/FloodMaps/flood_map_tick" + RunEnvironment.getInstance().getCurrentSchedule().getTickCount()  
	        									+ dateStamp + ".tif");
	        FileUtils.copyFile(floodMap_tiff, copy_floodMap_tiff);
	    } 
 	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	/*Impact assessment, in this case, is done simply by counting number of flooded houses. To reduce computation time, 
	 *only houses located below 60m contour line are checked if they are flooded. This is based on the extent of flood 
	 *with 150 year return period.
	 */
	public static void assessImpact() {
		/* Check if house is flooded. Number of houses flooded depends on the flood return period (flood extent) and location of houses. 
		 * Flood depth and extent for each each return period is read from hydrodynamic simulation results. 
		 * The centroid of the house (a point feature) is used to intersect the house with the flood map.
		 */
		double minElevationNotFlooded = 60;
		for(int i = 0; i < GlobalVariables.getExistingHouseholdList().size(); i++) {
			Household h = GlobalVariables.getExistingHouseholdList().get(i);
			if (h.getHouse().getElevation() < minElevationNotFlooded) {
				double floodDepth = physicalStructure.FloodMap.getFloodDepth(h.getHouse().getxyCoor());
				h.getHouse().setFloodDepth(floodDepth);
				// for elevated houses, also check if flood depth is greater than floor height
				double minFloodDepth = 0.05; // ASSUMPTION: a house is considered to be flooded if the flood depth is greater than 5cm.
				double maxFloodDepth = 2.0;  // ASSUMPTION: flood depth greater than 2m are most likely part of the waterbody. this assumption just is to make computation faster.
				if (floodDepth > minFloodDepth && floodDepth < maxFloodDepth  && floodDepth > h.getHouse().getElevated()) {  
					h.getHouse().setIsFlooded(1);  
					/* The direct tangible damage is calculated based on the method developed in CORFU project. The information needed to
					 * compute direct tangible damage are flood depth maps, land use maps of buildings and depth-damage curves based on 
					 * building uses. Flood depth based on return period, in this case, is obtained from the FloodMap class.The building use 
					 * information is stored in the shapefiles for existing files (StMaartenBuilder Class) or assigned in the buildHouse 
					 * method above in case of new houses. Depth-damage curves are given in arrays below and we linearly interpolate for 
					 * flood depth values in between the given points.   
					 */
					//double damage = interpLinear(floodDepth);
					//h.getHouse().setDamage(damage); // TODO physical damage calculation
					
					// Measures are implemented either per city or catchment or household level. Therefore, identify in which catchment the 
					// flooded house is located (no need to know the sub-catchment).  
					boolean isFloodedHouseInCatchment = false;
					for (int j = 0; j < StMaartenBuilder.getCatchmentGeom().size(); j++) {
						GeometryFactory geometryFactory = new GeometryFactory();
						isFloodedHouseInCatchment = geometryFactory.createPoint(h.getHouse().getxyCoor()).within(StMaartenBuilder.getCatchmentGeom().get(j));
						if (isFloodedHouseInCatchment) {
							String catchmentName = StMaartenBuilder.getCatchmentNameList().get(j);
							// merge smaller adjacent catchments to one as they are too small to implement a measure in each one 
							if (catchmentName.equals("BA") || catchmentName.equals("BB") || catchmentName.equals("BC")) {
								catchmentName = "B";
							} else if (catchmentName.equals("GC") || catchmentName.equals("GD") || catchmentName.equals("GF") || catchmentName.equals("GG")
									|| catchmentName.equals("GH") || catchmentName.equals("JA")) {
								catchmentName = "G";
							}
							GlobalVariables.getFrequency_floodedCatchmentNameList().add(catchmentName);
							//go out of the loop once the catchment is known
							break;
						}
					}
				}
			}
		}
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// This method converts a binary MIKE11 cross-section file (.xns11) to text file, updates the text file, and finally,
	// converts the text file back to an updated .xns11 file.
	public static void updateCrossectionFile(String catchment) {
		/**First, convert cross-section file (.xns11) to text file.*/
		// We assume that the level of protection (safety) that the structural FRM measures is reducing the risk of a flood from a rainfall 
		// magnitude of 50 years recurrence interval. To be sure that the measures satisfy the assumption, it is advisable to first model these 
		// measures (i.e., only hydrodynamic modelling). Once we know how much channel cross-sections should be widened to accommodate a flood
		// with 50 year recurrence interval, we use that file to update the respective channels that needs to be widened at a given time.
		// The xns11 file that holds all the changes to accommodate the 50 years flood is SXM_XNS_measuresAllChannels.xns11. We first convert 
		// this file to text file.
		Process p = null;
		ProcessBuilder pb = null;
		
		pb = new ProcessBuilder("data\\hydrodynamic_data\\SDK\\xns11ImExport.exe","export", 
				"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_measuresAllChannels.xns11", 
				"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_measuresAllChannels.txt");
		try {
			p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Convert the xns11 files used in the simulations
		if (check == 0) { // first time, use SXM_XNS.xns11
			pb = new ProcessBuilder("data\\hydrodynamic_data\\SDK\\xns11ImExport.exe",	"export", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS.xns11", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS.txt");
		} else if (check > 0) {  // once it is updated, use the improved .xns file, i.e., SXM_XNS_FRM50.xns11
			pb = new ProcessBuilder("data\\hydrodynamic_data\\SDK\\xns11ImExport.exe", "export", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_FRM50.xns11", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS.txt"); // no need to change the name of txt file
		}
		try {
			p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/**then, update the text file.*/
		// The name of files to open.
        String xnsTXT_fileName = "data/hydrodynamic_data/MIKE11/SXM_XNS.txt";  // readfile
        String xnsMeasuresAllChannelsTXT_fileName = "data/hydrodynamic_data/MIKE11/SXM_XNS_measuresAllChannels.txt";  // readfile
        String updatedXnsTXT_fileName = "data/hydrodynamic_data/MIKE11/SXM_XNS_updated.txt";  // writefile

        String lineSxmXns = null;  // we will read/write line-by-line from/to the text files.
        try {
        	// FileReader reads text files in the default encoding.
        	FileReader fileReaderXns = new FileReader(xnsTXT_fileName);
        	FileReader fileReaderXnsMeasuresAllChannels = new FileReader(xnsMeasuresAllChannelsTXT_fileName);
        	// FileWriter writes text files in the default encoding.
        	FileWriter fileWriter = new FileWriter(updatedXnsTXT_fileName);

        	// Always wrap FileReader in BufferedReader.
        	BufferedReader bufferedReaderXns = new BufferedReader(fileReaderXns);
        	BufferedReader bufferedReaderXnsAllMeasures = new BufferedReader(fileReaderXnsMeasuresAllChannels);
        	// Always wrap FileWriter in BufferedWriter.
        	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

        	// Read the total number of lines in the SXM_XNS.txt file
        	int totalNumberOfLinesSxmXns = 0;
        	Scanner readFileSxmXns = new Scanner(new File(xnsTXT_fileName));
        	while (readFileSxmXns.hasNext()) {
        		totalNumberOfLinesSxmXns++;
        		readFileSxmXns.nextLine();
        	}
        	readFileSxmXns.close();

        	// get the beginning and end of the channel that is going to be improved
        	ArrayList<Integer> beginEndLines = whichLinesAreChanged(xnsTXT_fileName, catchment, totalNumberOfLinesSxmXns);

        	for (int j = 0; j < totalNumberOfLinesSxmXns; j++) {
        		if (j == beginEndLines.get(0)) { // if j equals the begin line, go to the SXM_XNS_measuresAllChannels.txt
        			// Read the total number of lines in the SXM_XNS_measuresAllChannels.txt file
        			int totalNumberOfLinesSxmXnsAllMeasures = 0;
        			Scanner readFileSxmXnsAllMeasures = new Scanner(new File(xnsMeasuresAllChannelsTXT_fileName));
        			while (readFileSxmXnsAllMeasures.hasNext()) {
        				totalNumberOfLinesSxmXnsAllMeasures++;
        				readFileSxmXnsAllMeasures.nextLine();
        			}
        			readFileSxmXnsAllMeasures.close();

        			// read and write lines from the SXM_XNS_measuresAllChannels.txt where the channel name is the same as the catchment name
        			String lineSxmXnsAllMeasures = null;
        			for (int i = 0; i < totalNumberOfLinesSxmXnsAllMeasures; i++) {
        				lineSxmXnsAllMeasures = bufferedReaderXnsAllMeasures.readLine(); // read the header
        				String[] splitHeader = lineSxmXnsAllMeasures.split(",");  // split the header
    					int crossSectionPoints = Integer.parseInt(splitHeader[splitHeader.length-1]); // the last element defines Channel's shape at that chainage
        				String nameChannel = splitHeader[1];  // identify channel's name
        				Character ch1 = Character.toLowerCase(nameChannel.charAt(1)); // first character of the channel name
        				Character ch2 = Character.toLowerCase(catchment.charAt(0));  // character of the catchment
        				if (ch1 <= ch2) {
        					if (ch1 == ch2) {
            					bufferedWriter.write(lineSxmXnsAllMeasures); // write the header
            					bufferedWriter.newLine();
            					for (int n = 0; n < crossSectionPoints; n++) {
            						lineSxmXnsAllMeasures = bufferedReaderXnsAllMeasures.readLine(); // read the cross-sections
            						bufferedWriter.write(lineSxmXnsAllMeasures); // write the cross-sections
            						bufferedWriter.newLine();
            					}
            					// update the number-of-line counter for SXM_XNS_measuresAllChannels.txt
            					i = i + crossSectionPoints;
        					} else {
            					for (int n = 0; n < crossSectionPoints; n++) {
            						lineSxmXnsAllMeasures = bufferedReaderXnsAllMeasures.readLine(); // read the cross-sections
            					}
            					// update the number-of-line counter for SXM_XNS_measuresAllChannels.txt
            					i = i + crossSectionPoints;
        					}
        				} else {
        					break;
        				}
        			}

        			// skip the lines of the channels that are improved from SXM_XNS.txt
        			for (int k = beginEndLines.get(0); k < beginEndLines.get(1)+1; k++) {
        				lineSxmXns = bufferedReaderXns.readLine();
        			}
        			// update the number-of-line counter for SXM_XNS.txt
        			j = beginEndLines.get(1);
        		} else {
        			// read and write lines from the SXM_XNS.txt
        			lineSxmXns = bufferedReaderXns.readLine();  
        			bufferedWriter.write(lineSxmXns);
        			bufferedWriter.newLine();
        		}
        	}
    		// Close files.
        	bufferedReaderXns.close(); 
        	bufferedReaderXnsAllMeasures.close(); 
    		bufferedWriter.close();
        }
        catch(FileNotFoundException ex) {
        	System.out.println("Unable to open file '" + xnsTXT_fileName + "'");                
        }
        catch(IOException ex) {
        	System.out.println("Error reading file '" + xnsTXT_fileName + "'");                  
        	// Or we could just do this: 
        	// ex.printStackTrace();
        }
        
        /**Finally, convert the text file back to .xns11 file.*/
		if (check == 0) { // first time, use SXM_XNS.xns11
			pb = new ProcessBuilder("data\\hydrodynamic_data\\SDK\\xns11ImExport", "import", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_updated.txt", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS.xns11", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_FRM50.xns11");
		} else if (check > 0) {  // once it is updated, use the widened xns11 file, i.e., SXM_XNS_widened.xns11
			pb = new ProcessBuilder("data\\hydrodynamic_data\\SDK\\xns11ImExport", "import", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_updated.txt", 
					"data\\hydrodynamic_data\\MIKE11\\SXM_XNS_FRM50.xns11"); // just update SXM_XNS_FRM50
		}
		try {
			p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// now, update check
		check++; // TODO the position might change depending on proactive or reactive measures ... this case is reactive
	}
	
    /***********************************************************************************************
    ***********************************************************************************************/
	public static ArrayList<Integer> whichLinesAreChanged(String xnsTXT_fileName, String catchment, int totalNumberOfLines) {
		ArrayList<Integer> listBeginEnd = new ArrayList<Integer>();
		String line = null;  // we will read/write line-by-line from/to the text files
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(xnsTXT_fileName);
			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			int begin = 0;
			int end = 0;
			for (int j = 0; j < totalNumberOfLines; j++) {
				line = bufferedReader.readLine();  // read each line
				String[] splitHeader = line.split(",");  // split the header
				String nameChannel = splitHeader[1];  // identify channel's name
				Character channelChar = Character.toLowerCase(nameChannel.charAt(1)); // first character of the channel name
				Character catchmentChar = Character.toLowerCase(catchment.charAt(0));  // character of the catchment
				// The catchment and channel names are related (e.g., channel names in Catchment 'A' start with letter 'a'.
				// Since the channels are written in the xns.txt file alphabetically, if the first character of the channel name is greater than
				// that of the catchment name, there is no need to continue looping through the fileReader.
				if (channelChar <= catchmentChar) { // if the channels are not written alphabetically, this if-statement does not hold!
					if (channelChar == catchmentChar) {
						if (begin == 0) {
							begin = j; // copy the line number where the channel that needs to be updated starts 
							if (channelChar == 'a') begin = 0;
							if (channelChar == 'z') {
								end = totalNumberOfLines;
								break;
							}
						}
					}
					int crossSectionPoints = Integer.parseInt(splitHeader[splitHeader.length-1]); // the last element defines Channel's shape at that chainage
					for (int k = 0; k < crossSectionPoints; k++) {
						bufferedReader.readLine(); // skip lines
					}
					// update the number-of-line counter
					j = j + crossSectionPoints;
				} else {
					end = j-1; // copy the line number where the channel that needs to be updated ends
					break;
				}
			}
			// add the first and last line numbers of the 'relevant' channel to the list 
			listBeginEnd.add(begin);
			listBeginEnd.add(end);

			// Close files.
			bufferedReader.close(); 
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file '" + xnsTXT_fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file '" + xnsTXT_fileName + "'");                  
			// Or we could just do this: 
			// ex.printStackTrace();
		}
		return listBeginEnd;
	}

	/***********************************************************************************************
    ***********************************************************************************************/
	public static void updateMike21Bathymetry() {
		// The name of the files to open.
		String m21Copy_fileName = "data/hydrodynamic_data/MIKE21/St_Maarten_Full_Copy.m21";  // readfile
		String m21_fileName = "data/hydrodynamic_data/MIKE21/St_Maarten_Full.m21";  // writefile

		// This will reference one line at a time
		String line = null;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader = new FileReader(m21Copy_fileName);
			// FileWriter writes text files in the default encoding.
			FileWriter fileWriter = new FileWriter(m21_fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			int totalNumberOfLines = 571;  // The total number of lines in the sim11 file
			int changedBathymetryLines = 25; // The xns11 input file is written at the 23rd line (starting from 1) 
			for (int j = 0; j < totalNumberOfLines; j++) {   
				line = bufferedReader.readLine();
				if (j == changedBathymetryLines) {  // first time, use SXM_XNS.xns11
					// This format is copied from the sim11 file (be careful with the number of spaces!)
					bufferedWriter.write("               FILE_NAME = |.\\Bathymetry_St_Maarten_withroad30_Levee.dfs2|"); 
				} else {
					bufferedWriter.write(line);  // write the rest here
				}
				bufferedWriter.newLine();
			}   

			// Always close files.
			bufferedReader.close(); 
			bufferedWriter.close();
		}
		catch(FileNotFoundException ex) {
			System.out.println("Unable to open file: '" + m21_fileName + "' or '" + m21Copy_fileName + "'");                
		}
		catch(IOException ex) {
			System.out.println("Error reading file: '" + m21_fileName + "' or '" + m21Copy_fileName + "'");                  
		}		
	}
	
	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/
	public static int getCheck() {
		return check;
	}

	public static void setCheck(int check) {
		ABMFloodModelCouple.check = check;
	}

	public static int getFlagMike11() {
		return flagMike11;
	}

	public static void setFlagMike11(int flagMike11) {
		ABMFloodModelCouple.flagMike11 = flagMike11;
	}

	public static int getFlagMike21() {
		return flagMike21;
	}

	public static void setFlagMike21(int flagMike21) {
		ABMFloodModelCouple.flagMike21 = flagMike21;
	}

	public static double getTickLastStructuralMeasure() {
		return tickLastStructuralMeasure;
	}

	public static void setTickLastStructuralMeasure(double tickLastStructuralMeasure) {
		ABMFloodModelCouple.tickLastStructuralMeasure = tickLastStructuralMeasure;
	}

	public static List<Object> getSelectedCatchmentsSM() {
		return selectedCatchmentsSM;
	}

	public static void setSelectedCatchmentsSM(List<Object> selectedCatchmentsSM) {
		ABMFloodModelCouple.selectedCatchmentsSM = selectedCatchmentsSM;
	}
}
