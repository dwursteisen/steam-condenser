/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2008-2009, Sebastian Staudt
 */
package com.github.koraktor.steamcondenser.steam.community.tf2;

import org.w3c.dom.Element;

/**
 * The TF2ClassFactory is used to created instances of TF2Class based on the
 * XML input data
 * @author  Sebastian Staudt
 */
abstract class TF2ClassFactory
{

    final static String ENGINER_CLASS_NAME = "Engineer";
    final static String MEDIC_CLASS_NAME = "Medic";
    final static String SNIPER_CLASS_NAME = "Sniper";
    final static String SPY_CLASS_NAME = "Spy";
    
    /**
     * Creates a new instance of TF2Class storing the statistics for a Team
     * Fortress 2 class with the assigned XML data
     * @param classData
     * @return An instance of TF2Class or its subclasses TF2Engineer, TF2Medic,
     *         TF2Sniper or TF2Spy depending on the given XML data
     */
    public static TF2Class getTF2Class(Element classData)
    {
	String className = classData.getElementsByTagName("className").item(0).getTextContent();

	if(ENGINER_CLASS_NAME.equals(className))
	{
	    return new TF2Engineer(classData);
	}
	else if(MEDIC_CLASS_NAME.equals(className))
	{
	    return new TF2Medic(classData);
	}
	else if(SNIPER_CLASS_NAME.equals(className))
	{
	    return new TF2Sniper(classData);
	}
	else if(SPY_CLASS_NAME.equals(className))
	{
	    return new TF2Spy(classData);
	}
	else
	{
	    return new TF2Class(classData);
	}
    }
}
