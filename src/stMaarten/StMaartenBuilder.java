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
 
package stMaarten;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

//import org.apache.commons.math3.distribution.NormalDistribution;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import physicalStructure.Building;
import physicalStructure.Catchment;
import physicalStructure.CoastLine;
import physicalStructure.CoastLinePolygon;
import physicalStructure.Flood;
import physicalStructure.FloodZones;
import physicalStructure.House;
import physicalStructure.Plan;
import physicalStructure.Road;
import physicalStructure.Subcatchment;
import physicalStructure.WaterBody;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import collectiveStructure.Household;
import mainDataCollection.CatchmentsWithSM;
//import collectiveStructure.PermitDepartment;
import operationalStructure.ABMFloodModelCouple;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


/**
 * This class loads all geo-referenced files. Some of them are loaded for the sole purpose of display functionality.
 * Households, their houses and attributes, catchments, subcatchments, coastline, floodzones are initialised in this class.
 * 
 * Assumptions: 
 * 		- buildings, houses and developments represent the same thing, i.e., residential houses
 * 		- all the buildings are residential.
 * 		- all buildings are represented by a single point feature.
 * 
 * @param initialNumberOfHouseholds
 * @param initial_percentage_of_elevated_houses
 */
public class StMaartenBuilder implements ContextBuilder<Object> {

	public static Geography<Object> geography;
	public static Context<Object> context;
	
	private static List<Geometry> coastLineGeom = new ArrayList<Geometry>();  // this stores coast line geometry in an array, and used in the CoastLine class
	//private static List<Geometry> waterbodyGeom = new ArrayList<Geometry>();  // this stores all the waterbody polygons in one array, and used in the PermitDepartment class
	private static List<Geometry> subcatchmentGeom = new ArrayList<Geometry>();  // this stores all the subcatchment polygons geometry in one array, and used in the UrbanDevelopmentActionSituation class 
	private static List<String> subcatchmentNameList = new ArrayList<String>();  // this stores all the subcatchment polygons name in one array, and used in the UrbanDevelopmentActionSituation class (name order based on shapefile attribute table)
	private static List<String> subcatchmentNameListRR = new ArrayList<String>();  // this stores all the subcatchment polygons name in one array, and used in the UrbanDevelopmentActionSituation class (name order based on MIKE11 RR file)
	private static List<Double> subcatchmentCNList = new ArrayList<Double>();  // this stores the CN values of subcatchments (index corresponds to subcatchmentNameListRR). Values are updated in ABMFloodModelCouple.updateCN
	private static List<Geometry> catchmentGeom = new ArrayList<Geometry>();  // this stores all the catchment polygons geometry in one array, and used in the UrbanDevelopmentActionSituation class 
	private static List<String> catchmentNameList = new ArrayList<String>();  // this stores all the catchment polygons name in one array, and used in the UrbanDevelopmentActionSituation class 
	private static List<Geometry> floodZonesGeom = new ArrayList<Geometry>();  // this stores flood-prone areas geometry, and used in the UrbanDevelopmentActionSituation class 
	private static List<Double> floodZonesFloorHeight = new ArrayList<Double>(); // this stores floor heights in flood-prone areas, and used in the UrbanDevelopmentActionSituation class
	private static List<Geometry> developmentLocations = new ArrayList<Geometry>(); //this stores all the development location points (xy coordinate) in an array, and used in the UrbanDevelopmentActionSituation class
	private static List<Double> developmentLocations_elev = new ArrayList<Double>(); //this stores all the development location points (elevation) in an array, and used in the UrbanDevelopmentActionSituation class

	@Override
	public Context<Object> build(Context<Object> con) {
		// create geography projection
		context = con;
		GeographyParameters<Object> geoParams = new GeographyParameters<Object>();
		geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Geography", context, geoParams);

		// Clear static data members to freshly initialise the context
		coastLineGeom.clear();
		subcatchmentGeom.clear();
		subcatchmentNameList.clear();
		subcatchmentNameListRR.clear();
		subcatchmentCNList.clear();
		catchmentGeom.clear();
		catchmentNameList.clear();
		floodZonesGeom.clear();
		floodZonesFloorHeight.clear();
		developmentLocations.clear();
		developmentLocations_elev.clear();
		GlobalVariables.getExistingHouseholdList().clear();
		GlobalVariables.getFullExistingAndFutureDevelopmentLocations().clear();
		GlobalVariables.getList().clear();
		Flood.setReturnPeriod(0);
		ABMFloodModelCouple.setCheck(0);
		ABMFloodModelCouple.setFlagMike11(0);
		ABMFloodModelCouple.setFlagMike21(0);
		ABMFloodModelCouple.setTickLastStructuralMeasure(0);
		
	    // to avoid re-writing, delete SXM_XNS_FRM50.xns11 before starting simulations
	    File XNSfrm50 = new File("data\\hydrodynamic_data\\MIKE11\\SXM_XNS_FRM50.xns11");
        if (XNSfrm50.exists())
        	XNSfrm50.delete();

		// Load Features from shapefiles and add them (except all the building location point features) to the context and geography created above 
		loadFeatures("data/gis_data/CoastLine_polygon.shp", context, geography, 1);  // Whole island boundary (polygon)
		loadFeatures("data/gis_data/Waterbodies.shp", context, geography, 2);  // Sint Maarten water bodies (polygon)
		//loadFeatures("data/gis_data/building_all.shp", context, geography, 3); // All buildings on the island (polygon)
		loadFeatures("data/gis_data/Hydrological_Subcatchments.shp", context, geography, 4); // Subcatchments for rainfall-runoff model (polygon)
		loadFeatures("data/gis_data/Hydrological_Catchments.shp", context, geography, 5); // Catchments for rainfall-runoff model (polygon)
		loadFeatures("data/gis_data/FloodproneArea.shp", context, geography, 6); // Flood-prone areas, based on the development plan (polygon)
		loadFeatures("data/gis_data/building_all_Points.shp", context, geography, 0); // All buildings on the island (point)
		loadFeatures("data/gis_data/New_Development_points.shp", context, geography, 1); // New development locations, perhaps in the last 5 years, on the island (point)
		loadFeatures("data/gis_data/DevelopmentLocations_Future.shp", context, geography, 1); // New development locations, in the next decade, on the island (point)
		//loadFeatures("data/gis_data/CriticalBuildings.shp", context, geography, 1);  // Critical buildings (point)
		//loadFeatures("data/gis_data/sint_maarten_highway.shp", context, geography, 0); // Highways (polyline) - only for display
		loadFeatures("data/gis_data/CoastLine_single.shp", context, geography, 1); // Coast line (polyline) - only for display
		
		// in the above loadfeature() method, we just saved all the building locations in a list. In this method, we initialise households and their houses
		// and add them both to the context and geography.
		initialiseHouseholds();	
		
		// initialise the CatchmentsWithSM class with an object of "null" catchmentName and add it to the context
		CatchmentsWithSM iniCatchmentWithSM = new CatchmentsWithSM("null");
		StMaartenBuilder.context.add(iniCatchmentWithSM);
		ABMFloodModelCouple.getSelectedCatchmentsSM().add(iniCatchmentWithSM);
		
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());  // this line solves the problem of JAI Libraries issue

		// Read two text files of catchment names and corresponding "present-situation" CN values. The order of catchment names read from the shapefile 
		// above is different from the one given in the RR file of MIKE11. Hence, the text files are created as a work-around to match the RR file. 
		readFile("data/hydrodynamic_data/catchment.txt", 1);
		readFile("data/hydrodynamic_data/original_CN.txt", 2);
		
		// Terminate the simulation after 30 ticks
		RunEnvironment.getInstance().endAt(30);
		
		return context;
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	private void loadFeatures (String filename, Context<Object> context, Geography<Object> geography, int flag){
		
		// Locate the shapefile
		URL url = null;
		try {
			url = new File(filename).toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Try to load the shapefiles
		SimpleFeatureIterator fiter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);

		try {
			fiter = store.getFeatureSource().getFeatures().features();

			while(fiter.hasNext()){
				features.add(fiter.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		finally{
			fiter.close();
			store.dispose();
		}
		
		// For each feature in the file
		for (SimpleFeature feature : features){
			Geometry geom = (Geometry)feature.getDefaultGeometry();

			// For Polygons, create whole island boundary, water bodies on the island, existing buildings and subcatchments for rainfall-runoff model
			if (geom instanceof MultiPolygon){
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);

				// Read the feature attributes and assign to the respective polygon feature
				if (flag == 1) {
					CoastLinePolygon coastLine = new CoastLinePolygon();
					context.add(coastLine);
					geography.move(coastLine, geom);
				} else if (flag == 2) {
					WaterBody waterbody = new WaterBody();
					context.add(waterbody);
					geography.move(waterbody, geom);
					//waterbodyGeom.add(geom);
				} else if (flag == 3) {
					Building building = new Building();
					context.add(building);
					geography.move(building, geom);
				} else if (flag == 4) {
					String subcatchmentName = (String)feature.getAttribute("NAME");
					Subcatchment subcatchment = new Subcatchment(subcatchmentName);
					context.add(subcatchment);
					//geography.move(subcatchment, geom);
					// the indices of the two lists refer to the same feature's geometry and name (used in UrbanDevelopmentActionSituation.developmentSubcatchment())
					subcatchmentGeom.add(geom);
					subcatchmentNameList.add(subcatchmentName);
				} else if (flag == 5) {
					String catchmentName = (String)feature.getAttribute("NAME");
					Catchment catchment = new Catchment(catchmentName);
					context.add(catchment);
					//geography.move(catchment, geom);
					catchmentGeom.add(geom);
					catchmentNameList.add(catchmentName);
				} else if (flag == 6) {
					double floorHeight = (Double)feature.getAttribute("Elevated");
					FloodZones floodZone = new FloodZones(floorHeight);
					context.add(floodZone);
					geography.move(floodZone, geom);
					floodZonesGeom.add(geom);
					floodZonesFloorHeight.add(floorHeight);
				} else {
					System.out.println("Define flag!" + filename);
				}
			}

			// For Points, create houses which are owned by households (one-to-one relationship between houses and households)
			// or load future development locations
			else if (geom instanceof Point){
				geom = (Point)feature.getDefaultGeometry();
				
				//here, only populate a list with households. The initialisation happens later.
				if (flag == 0) {
					// Read the feature attributes and assign to the houses characteristics
					//String buildingFunction = (String)feature.getAttribute("Fac_Type"); // get the building function attribute from shapefile
					double elevation = (double)feature.getAttribute("Elevation1");  // get the elevation attribute from shapefile
					/** @assumption: all the buildings are residential. */
					String buildingFunction = "Residential";  
					// Some attributes are not still available and hence the following initial values are used
					int initialIsFloodedHouse = 0; // at t=0, no house is flooded
					// initialise 'elevated' with zero and updated the value in initialiseHouseholds() method below
					double elevated = 0;
					/** @assumption: all buildings are represented by a single point feature.*/
					Coordinate xyCoor = geom.getCoordinate();
					String xyCoorString = xyCoor.toString();
					double floodDepth = 0;
					//initialise houses with the values above. The last three attributes are initialised with default values (NA refers to Not Applicable).
					House iniHouse = new House(buildingFunction, elevation, initialIsFloodedHouse, elevated, xyCoor, "NA", "NA", "NA", xyCoorString, floodDepth);
					//initialise plans with default values. NA refers to Not Applicable
					Plan iniPlan = new Plan(null, 0, 0, "NA", "NA", "NA");

					// create households using the above attributes
					Household existingHousehold = new Household(iniHouse, iniPlan);
					GlobalVariables.getFullExistingAndFutureDevelopmentLocations().add(existingHousehold);

				} else if (flag == 1) {
					double elevation = (double)feature.getAttribute("Elevation1");
					developmentLocations.add(geom);
					developmentLocations_elev.add(elevation);
				} else {
					System.out.println("Define flag!" + filename);
				}
			}

			// For Lines, create Highways
			else if (geom instanceof MultiLineString){
				MultiLineString line = (MultiLineString)feature.getDefaultGeometry();
				geom = (LineString)line.getGeometryN(0);

				if (flag == 0) {
					// Read the feature attributes and assign to the highways
					//String type = (String)feature.getAttribute("TYPE");
					Road road = new Road();
					context.add(road);
					geography.move(road, geom);
				} else if (flag == 1) {
					CoastLine coastLine = new CoastLine();
					coastLineGeom.add(geom);
					context.add(coastLine);
					// No need to move this object to geography. For visualisation, CoastLine_polygon.shp is used.
				} else {
					System.out.println("Define flag!" + filename);
				}
			} 
			
			// If geometry is undefined or if there is any issue with the geometry, print error.
			else {
				System.out.println("Error creating agent for  " + geom);
			}
		}				
	}
	
	/***********************************************************************************************
	***********************************************************************************************/
	// This method initialises households
	private void initialiseHouseholds() {
		/** @params: initialNumberOfHouseholds
		 * This parameter defines the initial number of agents. The default value is based on number of houses in a building
		 * shapefile dated before 2009 (actual year is not known)
		 * 
		 * The parameter is set in Repast Simphony Runtime Environment as "Initial Number Of Households" and is an integer number.
		 */
		Parameters params = RunEnvironment.getInstance().getParameters();
		int initialNumberOfHouseholds = (int)params.getValue("initialNumberOfHouseholds");
		// MonteCarlo simulation: normal distribution
		/*double initialNumberOfHouseholds = 0;
		NormalDistribution normal = new NormalDistribution(12000, 1000);
		initialNumberOfHouseholds = normal.sample();*/
		
		// shuffle the list with a seed
		//int randomSeed = (int)params.getValue("randomSeed");
		//Collections.shuffle(GlobalVariables.getFullExistingAndFutureDevelopmentLocations(), new Random(randomSeed));
		Collections.shuffle(GlobalVariables.getFullExistingAndFutureDevelopmentLocations(), new Random(1000));
		
		for (int j = 0; j < GlobalVariables.getFullExistingAndFutureDevelopmentLocations().size(); j++) {
			if (j < initialNumberOfHouseholds) {
				Household h = GlobalVariables.getFullExistingAndFutureDevelopmentLocations().get(j);
				
				/** @params: initial_percentage_of_elevated_houses
				 * This parameter defines the initial percentage of elevated houses in the island. The default value is based on  
				 * expert estimation and observation. 
				 * 
				 * The parameter is set in Repast Simphony Runtime Environment as "Initial Percentage Of elevated houses" and is a double [0,1].
				 * 
				 * This implementation does not guarantee that 80% of houses are elevated! TODO: how to improve that?
				 */
				double initialPercentageOfElevatedHouses = (double)params.getValue("initial_percentage_of_elevated_houses");
				double floorHeight = 0.2; // this is the default value of floor elevations
				double random = RandomHelper.nextDouble();
				if (random <= initialPercentageOfElevatedHouses) {
					h.getHouse().setElevated(floorHeight);
					h.getHouse().setComplianceBO("yes"); // the default is NA
				}
				GlobalVariables.getExistingHouseholdList().add(h);
				// add the household, the house and the plan in the context and the geography (no need to move the "plan" to the "geography")
				context.add(h);
				context.add(h.getHouse());
				context.add(h.getPlan());
				GeometryFactory geometryFactory = new GeometryFactory();
				geography.move(h.getHouse(), geometryFactory.createPoint(h.getHouse().getxyCoor()));
				geography.move(h, geometryFactory.createPoint(h.getHouse().getxyCoor()));
			} else {  // store the rest in development locations list
				GeometryFactory geometryFactory = new GeometryFactory();
				developmentLocations.add(geometryFactory.createPoint(GlobalVariables.getFullExistingAndFutureDevelopmentLocations().get(j).getHouse().getxyCoor()));
				developmentLocations_elev.add(GlobalVariables.getFullExistingAndFutureDevelopmentLocations().get(j).getHouse().getElevation());
			}
		}
	}

	/***********************************************************************************************
	***********************************************************************************************/
	public void readFile(String filename, int flag) {
        // The name of the file to open.
        String fileName = filename;

        // This will reference one line at a time
        String line = null;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	if (flag == 1) {
                    //System.out.println(line);
                    subcatchmentNameListRR.add(line);
            	}
            	if (flag == 2) {
                    //System.out.println(line);
                    subcatchmentCNList.add(Double.parseDouble(line));
            	}
            }   

            // Always close files.
            bufferedReader.close(); 
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                "Unable to open file '" + 
                fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println(
                "Error reading file '" 
                + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }		
	}

	/***********************************************************************************************
	*********************************** Getters and Setters ****************************************
	***********************************************************************************************/
	public static List<Geometry> getCoastLineGeom() {
		return coastLineGeom;
	}

	public static void setCoastLineGeom(List<Geometry> coastLineGeom) {
		StMaartenBuilder.coastLineGeom = coastLineGeom;
	}

	public static List<Geometry> getSubcatchmentGeom() {
		return subcatchmentGeom;
	}

	public static void setSubcatchmentGeom(List<Geometry> subcatchmentGeom) {
		StMaartenBuilder.subcatchmentGeom = subcatchmentGeom;
	}

	public static List<String> getSubcatchmentNameList() {
		return subcatchmentNameList;
	}

	public static void setSubcatchmentNameList(List<String> subcatchmentNameList) {
		StMaartenBuilder.subcatchmentNameList = subcatchmentNameList;
	}

	public static List<String> getSubcatchmentNameListRR() {
		return subcatchmentNameListRR;
	}

	public static void setSubcatchmentNameListRR(List<String> subcatchmentNameListRR) {
		StMaartenBuilder.subcatchmentNameListRR = subcatchmentNameListRR;
	}

	public static List<Double> getSubcatchmentCNList() {
		return subcatchmentCNList;
	}

	public static void setSubcatchmentCNList(List<Double> subcatchmentCNList) {
		StMaartenBuilder.subcatchmentCNList = subcatchmentCNList;
	}

	public static List<Geometry> getCatchmentGeom() {
		return catchmentGeom;
	}

	public static void setCatchmentGeom(List<Geometry> catchmentGeom) {
		StMaartenBuilder.catchmentGeom = catchmentGeom;
	}

	public static List<String> getCatchmentNameList() {
		return catchmentNameList;
	}

	public static void setCatchmentNameList(List<String> catchmentNameList) {
		StMaartenBuilder.catchmentNameList = catchmentNameList;
	}

	public static List<Geometry> getFloodZonesGeom() {
		return floodZonesGeom;
	}

	public static void setFloodZonesGeom(List<Geometry> floodZonesGeom) {
		StMaartenBuilder.floodZonesGeom = floodZonesGeom;
	}

	public static List<Double> getFloodZonesFloorHeight() {
		return floodZonesFloorHeight;
	}

	public static void setFloodZonesFloorHeight(List<Double> floodZonesFloorHeight) {
		StMaartenBuilder.floodZonesFloorHeight = floodZonesFloorHeight;
	}

	public static List<Geometry> getDevelopmentLocations() {
		return developmentLocations;
	}

	public static void setDevelopmentLocations(List<Geometry> developmentLocations) {
		StMaartenBuilder.developmentLocations = developmentLocations;
	}

	public static List<Double> getDevelopmentLocations_elev() {
		return developmentLocations_elev;
	}

	public static void setDevelopmentLocations_elev(List<Double> developmentLocations_elev) {
		StMaartenBuilder.developmentLocations_elev = developmentLocations_elev;
	}
}
