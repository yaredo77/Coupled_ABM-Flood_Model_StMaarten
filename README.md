# Coupled_ABM-Flood_Model
This coupled ABM-Flood model is based on the modelling framework called Coupled fLood-Agent-Institution Modelling framework (CLAIM). CLAIM integrates actors, institutions, the urban environment, hydrologic and hydrodynamic processes and external factors that affect local flood risk management (FRM) activities. The framework conceptualizes the complex interaction of floods, humans and their environment as drivers of flood hazard, vulnerability and exposure.

The human subsystem is modelled using the agent-based modelling approach (ABM). As such, it incorporates heterogeneous actors and their actions on and interactions with their environment and flood. It also provides the possibility to analyze the underlying institutions that govern the actions and interactions in managing flood risk by incorporating MAIA (Modelling Agent systems using Institutional Analysis) meta-model (Ghorbani et al., 2013). The flood subsystem is modelled using physically-based, numerical flood modelling software. Then, the ABM is coupled with the flood model dynamically to understand how humans and their environment interact, to experiment the effect of different institutions and to investigate FRM policy options.

The code is structured based on MAIA, and it is developed in the Repast Simphony (RS) development environment (https://repast.github.io/). The model is developed for the FRM case of the Caribbean Island of Sint Maarten. Unfortunately, some datasets used for the ABM and the coupled flood model (developed in the commercial MIKE FLOOD software - https://www.mikepoweredbydhi.com/download/mike-2017) are not provided because of restricted access. However, since the coupling is done within the ABM code, one can get a good idea of how ABMs can be coupled with domain models, how to handle data exchange between the two and how institutions are structured and incorporated in ABMs.

The code has five packages:
1.	Collective structure
2.	Operational structure
3.	Physical structure
4.	Main data collection
5.	Context builder (StMaarten)

The first three packages are based on the MAIA structure – the collective and physical structures create the agent and object classes while the operational structure stores three action situation classes. These classes define the most important human and flood dynamics. The other two packages are based on the RS architecture. The StMaarten builder package implements model initialization and the MainDataCollection package implements result collection and creating csv outputs.

This work is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.

References

Ghorbani, A., Bots, P., Dignum, V., Dijkema, G., 2013. MAIA: a Framework for Developing Agent-Based Social Simulations. JASSS 16, 9. https://doi.org/10.18564/jasss.2166
