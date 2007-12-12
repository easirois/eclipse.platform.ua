/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.intro.anchors;

/**
 * Test that the order in which extensions are processed does not matter
 * In the test case extn1 and extn2 contribute to the root
 * extn3 contributes to page 1
 * extn 4 contributes to page 2
 * extn5 contributes to page 4
 */

import java.util.Vector;

import junit.framework.TestCase;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.internal.intro.impl.model.AbstractIntroElement;
import org.eclipse.ui.internal.intro.impl.model.IntroHomePage;
import org.eclipse.ui.internal.intro.impl.model.IntroModelRoot;
import org.eclipse.ui.internal.intro.impl.model.IntroPage;
import org.eclipse.ui.internal.intro.impl.model.loader.ModelLoaderUtil;

public class ExtensionReorderingTest extends TestCase {
	
	private IConfigurationElement config;
	private IConfigurationElement[] introConfigExtensions;
	
	private class Permutations {
		private int numContributions;
		private int[] order;
		
		public void testAll(int numContributions) {
			this.numContributions = numContributions;
			order = new int[numContributions];
			tryAll(0);
		}
		
		/*
		 * Recursive test to test all permutations of integers 0-4 with no
		 * repeats. 
	     * @param next the next element that has not been filled in yet
		 */
		private void tryAll(int next) {
			for (int value = 0; value < numContributions; value++) {
				tryValue(next, value);		
			}
		}

		private void tryValue(int next, int value) {
			// Check to see if this is already in the array
			for (int pos = 0; pos < next; pos++) {
				if (order[pos] == value) {
					return;
				}
			}
			// Not already there
			order[next] = value;
			if (next + 1 == numContributions) {
				testReordering(order);
			} else {
				tryAll(next + 1);
			}
		}
		
		private String toString(int[] order) {
			String result = "";
			for (int i = 0; i < order.length; i++) {
				result = result + order[i];
			}
			return result;
		}

		private void testReordering(int[] order) {
			//System.out.println("Try " + toString(order));
			readIntroConfig();
			IConfigurationElement[] extensions = new IConfigurationElement[numContributions];
			for (int i = 0; i < numContributions; i++) {
				extensions[i] = introConfigExtensions[order[i]];
			}
			IntroModelRoot model = new IntroModelRoot(config, extensions);
			
			try {
				model.loadModel();
				assertTrue("Order = " + toString(order), model.hasValidConfig());
				checkModel(model, numContributions);
			} catch (RuntimeException e) {
				e.printStackTrace();
				fail("Exception thrown when order was " + toString(order));
			}
		}	
	}

	public void readIntroConfig() {
		 IExtensionRegistry registry= Platform.getExtensionRegistry();
	     IConfigurationElement[] configElements = registry
	            .getConfigurationElementsFor("org.eclipse.ui.intro.config");

	    config = getConfigurationFromAttribute(
	            configElements, "introId", "org.eclipse.ua.tests.intro.anchors");

        introConfigExtensions = null;

            introConfigExtensions = getIntroConfigExtensions(
                "configId", "org.eclipse.ua.tests.intro.config.anchors");
	}
	 
	private IConfigurationElement getConfigurationFromAttribute(
	            IConfigurationElement[] configElements, String attributeName,
	            String attributeValue) {

	        // find all configs with given attribute and attribute value.
	        IConfigurationElement[] filteredConfigElements = getConfigurationsFromAttribute(
	            configElements, attributeName, attributeValue);
	        // now validate that we got only one.
	        IConfigurationElement config = ModelLoaderUtil
	            .validateSingleContribution(filteredConfigElements, attributeName);
	        return config;
	}
	
	protected IConfigurationElement[] getConfigurationsFromAttribute(
            IConfigurationElement[] configElements, String attributeName,
            String attributeValue) {

        // find all configs with given attribute and attribute value.
        Vector elements = new Vector();
        for (int i = 0; i < configElements.length; i++) {
            String currentAttributeValue = configElements[i]
                .getAttribute(attributeName);
            if (currentAttributeValue != null
                    && currentAttributeValue.equals(attributeValue))
                elements.add(configElements[i]);
        }

        // now return array.
        IConfigurationElement[] filteredConfigElements = new IConfigurationElement[elements
            .size()];
        elements.copyInto(filteredConfigElements);

        return filteredConfigElements;
    }
	
	protected IConfigurationElement[] getIntroConfigExtensions(
            String attrributeName, String attributeValue) {

		IExtensionRegistry registry= Platform.getExtensionRegistry();
        IConfigurationElement[] configExtensionElements = registry
            .getConfigurationElementsFor("org.eclipse.ui.intro.configExtension");
  

        IConfigurationElement[] configExtensions = getConfigurationsFromAttribute(
            configExtensionElements, attrributeName, attributeValue);

        return configExtensions;
    }

	public void testOrder12345() {
		readIntroConfig();
		assertNotNull(config);
		assertEquals(5, introConfigExtensions.length);
		IntroModelRoot model = new IntroModelRoot(config, introConfigExtensions);
		model.loadModel();
		checkModel(model, 5);
	}

	private void checkModel(IntroModelRoot model, int elements) {
		assertTrue(model.hasValidConfig());	
		Object[] pages = model.getChildrenOfType(AbstractIntroElement.ABSTRACT_PAGE);
		IntroHomePage root = (IntroHomePage) model.findChild("root");
		assertEquals(elements + 2, pages.length);
		IntroPage extn1 = (IntroPage) model.findChild("page1");
		assertNotNull(extn1);
		AbstractIntroElement p1link = root.findChild("page1link");
		assertNotNull(p1link);
		IntroPage extn2 = (IntroPage) model.findChild("page2");
		assertNotNull(extn2);
		AbstractIntroElement p2link = root.findChild("page2link");
		assertNotNull(p2link);
		IntroPage extn3 = (IntroPage) model.findChild("page3");
		assertNotNull(extn3);
		AbstractIntroElement p3link = extn1.findChild("page3link");
		assertNotNull(p3link);
		if (elements >= 4) {
		    IntroPage extn4 = (IntroPage) model.findChild("page4");
		    assertNotNull(extn4);
			AbstractIntroElement p4link = extn2.findChild("page4link");
			assertNotNull(p4link);
		    if (elements >= 5) {
		        IntroPage extn5 = (IntroPage) model.findChild("page5");
		        assertNotNull(extn5);
				AbstractIntroElement p5link = extn4.findChild("page5link");
				assertNotNull(p5link);
		    }
		}
	}

	public void testAllOrdersOf3Contributions() {
		new Permutations().testAll(3);
	}
	
	public void testAllOrdersOf4Contributions() {
		new Permutations().testAll(4);
	}
	
	public void testAllOrdersOf5Contributions() {
		readIntroConfig();
		new Permutations().testAll(5);
	}		
	
}