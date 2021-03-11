package ca.mcgill.ecse.flexibook.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.mcgill.ecse.flexibook.model.*;

public class TOServiceCombo {
	//------------------------
	  // MEMBER VARIABLES
	  //------------------------

	  //TOService Attributes
	  private String name;
	  private ComboItem mainService;
	  private List<ComboItem> services;
	  

	  //------------------------
	  // CONSTRUCTOR
	  //------------------------

	  public TOServiceCombo(String aName, ComboItem aMainService){
	    name = aName;
	    services = new ArrayList<ComboItem>();
	    mainService = aMainService;
	  }

	  //------------------------
	  // INTERFACE
	  //------------------------

	  public boolean setName(String aName)
	  {
	    boolean wasSet = false;
	    name = aName;
	    wasSet = true;
	    return wasSet;
	  }

	  public String getName()
	  {
	    return name;
	  }

	  public int indexOfService(ComboItem aService)
	  {
	    int index = services.indexOf(aService);
	    return index;
	  }
	  
	  public ComboItem getMainService()
	  {
	    return mainService;
	  }
	  
	  public List<ComboItem> getServices()
	  {
	    List<ComboItem> newServices = Collections.unmodifiableList(services);
	    return newServices;
	  }

	  
	  public void delete()
	  {}


	  public String toString()
	  {
	    return super.toString() + "["+
	            "name" + ":" + getName()+ "," +
	            "main service" + ":" + getMainService()+ "," + 
	            "list of services" + ":" + getServices() + "]";
	  }
}
