/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.intro.impl.parts;

import java.io.*;
import java.net.*;
import java.util.*;

import org.eclipse.swt.graphics.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.intro.impl.model.*;
import org.eclipse.ui.internal.intro.impl.util.*;
import org.osgi.framework.*;

public class PageStyleManager {

    private Properties pageProperties;
    private Hashtable altStyleProperties = new Hashtable();
    private AbstractIntroPage page;
    private Bundle bundle;
    private IntroModelRoot root;

    /**
     * Constructor used when shared styles need to be loaded. The bundle is
     * retrieved from the model root.
     * 
     * @param modelRoot
     */
    public PageStyleManager(IntroModelRoot modelRoot) {
        bundle = modelRoot.getBundle();
        pageProperties = new Properties();
        String sharedStyle = modelRoot.getPresentation()
                .getImplementationStyle();
        if (sharedStyle != null)
                load(pageProperties, sharedStyle);
    }

    /**
     * Constructor used when page styles need to be loaded. The plugin's bundle
     * is retrieved from the page model class. The default properties are
     * assumed to be the presentation shared properties. The inherrited
     * properties are properties that we got from included and extension styles.
     * 
     * @param modelRoot
     */
    public PageStyleManager(AbstractIntroPage page, Properties sharedProperties) {
        this.page = page;
        bundle = page.getBundle();
        pageProperties = new Properties(sharedProperties);
        String altStyle = page.getAltStyle();
        if (altStyle != null)
                load(pageProperties, altStyle);

        // AltStyles hashtable has alt-styles as keys, the bundles as
        // values.
        Hashtable altStyles = page.getAltStyles();
        Enumeration styles = altStyles.keys();
        while (styles.hasMoreElements()) {
            String style = (String) styles.nextElement();
            Properties inheritedProperties = new Properties();
            Bundle bundle = (Bundle) altStyles.get(style);
            load(inheritedProperties, style);
            altStyleProperties.put(inheritedProperties, bundle);
        }

        // cache root
        root = (IntroModelRoot) page.getParentPage().getParent();
    }

    private void load(Properties properties, String style) {
        if (style == null)
                return;
        try {
            URL styleURL = new URL(style);
            InputStream is = styleURL.openStream();
            properties.load(is);
            is.close();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    public String getProperty(String key) {
        Properties aProperties = findProperty(key);
        return aProperties.getProperty(key);
    }


    /**
     * Finds a Properties that represents an inherited shared style, or this
     * current pages style.
     * 
     * @param key
     * @return
     */
    private Properties findProperty(String key) {
        // search inherited properties first.
        Enumeration inheritedPageProperties = altStyleProperties.keys();
        while (inheritedPageProperties.hasMoreElements()) {
            Properties aProperties = (Properties) inheritedPageProperties
                    .nextElement();
            if (aProperties.containsKey(key))
                    return aProperties;
        }
        // search the page and shared properties last.
        return pageProperties;
    }

    /**
     * Finds the bundle from which as shared style was loaded.
     * 
     * @param key
     * @return
     */
    private Bundle getAltStyleBundle(String key) {
        Properties aProperties = findProperty(key);
        return (Bundle) altStyleProperties.get(aProperties);
    }

    private RGB getRGB(String key) {
        String value = getProperty(key);
        if (value == null)
                return null;
        if (value.charAt(0) == '#') {
            // HEX
            try {
                int r = Integer.parseInt(value.substring(1, 3), 16);
                int g = Integer.parseInt(value.substring(3, 5), 16);
                int b = Integer.parseInt(value.substring(5, 7), 16);
                return new RGB(r, g, b);
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    /**
     * 
     * 
     * @param toolkit
     * @param key
     * @return color. May return null.
     */
    public Color getColor(FormToolkit toolkit, String qualifier) {
        FormColors colors = toolkit.getColors();
        String key = createColorKey(page, qualifier);
        Color color = colors.getColor(key);
        if (color == null) {
            RGB rgb = getRGB(key);
            if (rgb != null)
                    color = colors.createColor(key, rgb);
        }
        return color;
    }

    private String createColorKey(AbstractIntroPage page, String qualifier) {
        if (page != null)
                return StringUtil.concat(page.getId(), ".", qualifier); //$NON-NLS-1$
        return qualifier;
    }

    /**
     * Retrieves an image for a link in a page. If not found, uses the page's
     * default link image. If still not found, uses the passed default.
     * 
     * @param link
     * @param qualifier
     * @return
     */
    public Image getImage(IntroLink link, String qualifier, String defaultKey) {
        String key = createImageKey(page, link, qualifier);
        String pageKey = createImageKey(page, null, qualifier);
        return getImage(key, pageKey, defaultKey);
    }

    private String createImageKey(AbstractIntroPage page, IntroLink link,
            String qualifier) {
        StringBuffer buff = new StringBuffer();
        buff.append(page.getId());
        if (link != null) {
            buff.append("."); //$NON-NLS-1$
            buff.append(link.getId());
        }
        buff.append("."); //$NON-NLS-1$
        buff.append(qualifier);
        return buff.toString();
    }

    /**
     * Retrieve an image from this page's properties, given a key.
     * 
     * @param key
     * @param defaultPageKey
     * @param defaultKey
     * @return
     */
    public Image getImage(String key, String defaultPageKey, String defaultKey) {
        String currentKey = key;
        String value = getProperty(currentKey);
        if (value == null && defaultPageKey != null) {
            currentKey = defaultPageKey;
            value = getProperty(defaultPageKey);
        }
        if (value != null) {
            if (ImageUtil.hasImage(currentKey))
                    return ImageUtil.getImage(currentKey);
            // try to register the image.
            Bundle bundle = getAltStyleBundle(currentKey);
            if (bundle == null)
                    // it means that we are getting a key defined in this page's
                    // styles. (ie: not an inherited style).
                    bundle = this.bundle;
            ImageUtil.registerImage(currentKey, bundle, value);
            Image image = ImageUtil.getImage(currentKey);
            if (image != null)
                    return image;
        }
        // try default
        if (defaultKey != null)
                return ImageUtil.getImage(defaultKey);
        return null;
    }

    /**
     * @return Returns the properties.
     */
    protected Properties getProperties() {
        return pageProperties;
    }

    public int getPageNumberOfColumns() {
        String key = page.getId() + ".layout.ncolumns"; //$NON-NLS-1$
        int ncolumns = 0;
        String value = getProperty(key);
        try {
            ncolumns = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return ncolumns;
    }


    public int getNumberOfColumns(IntroDiv group) {
        String key = page.getId() + "." + group.getId() + ".layout.ncolumns"; //$NON-NLS-1$ //$NON-NLS-2$
        return getIntProperty(key);
    }

    private int getIntProperty(String key) {
        int ncolumns = 0;
        String value = getProperty(key);
        try {
            ncolumns = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return ncolumns;
    }


    public int getVerticalLinkSpacing() {
        String key = page.getId() + ".layout.link-vspacing"; //$NON-NLS-1$
        int vspacing = 5;
        String value = getProperty(key);
        try {
            vspacing = Integer.parseInt(value);
        } catch (NumberFormatException e) {
        }
        return vspacing;
    }


    /**
     * Finds the description text of the given group. Looks for the Text child
     * element whos id is specified as follows:
     * <p>
     * <pageId>. <path_to_group>.description-id= <id of child description Text
     * element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getDescription(IntroDiv group) {
        String key = page.getId() + "." + group.getId() + ".description-id";
        return doGetDescription(group, key);
    }

    /**
     * Finds the description text of the associated page. Looks for the Text
     * child element whos id is specified as follows:
     * <p>
     * <pageId>.description-id= <id of child description Text element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getPageDescription() {
        String key = page.getId() + ".description-id";
        return doGetDescription(page, key);
    }

    private String doGetDescription(AbstractIntroContainer parent, String key) {
        String path = getProperty(key);
        String description = null;
        if (path != null)
                description = findTextFromPath(parent, path);
        if (description != null)
                return description;
        return findTextFromStyleId(parent, getDescriptionStyleId());
    }

    private String getDescriptionStyleId() {
        String key = "description-style-id";
        return getProperty(key);
    }

    /**
     * Finds the subtitle of the associated page. Looks for the Text child
     * element whose id is specified as follows:
     * <p>
     * <pageId>.description-id= <id of child description Text element>
     * </p>
     * If not found, use the default description style.
     * 
     * @param group
     * @return
     */
    public String getPageSubTitle() {
        String key = page.getId() + ".subtitle-id";
        String path = getProperty(key);
        String description = null;
        if (path != null)
                description = findTextFromPath(page, path);
        if (description != null)
                return description;
        return findTextFromStyleId(page, getPageSubTitleStyleId());
    }

    private String getPageSubTitleStyleId() {
        String key = "subtitle-style-id";
        return getProperty(key);
    }

    private String findTextFromPath(AbstractIntroContainer parent, String path) {
        AbstractIntroElement child = parent.findTarget(root, path);
        if (child != null && child.isOfType(AbstractIntroElement.TEXT))
                return ((IntroText) child).getText();
        return null;
    }

    /**
     * Returns the first direct child text element with the given style-id.
     * 
     * @return
     */
    private String findTextFromStyleId(AbstractIntroContainer parent,
            String styleId) {
        IntroText[] allText = (IntroText[]) parent
                .getChildrenOfType(AbstractIntroElement.TEXT);
        for (int i = 0; i < allText.length; i++) {
            if (allText[i].getClassId().equals(styleId))
                    return allText[i].getText();
        }
        return null;
    }



    private String findTextFromIdXX(AbstractIntroContainer parent,
            String childId) {
        AbstractIntroElement child = parent.findChild(childId);
        if (child != null && child.isOfType(AbstractIntroElement.TEXT))
                return ((IntroText) child).getText();
        return null;
    }

    /**
     * Returns the first child text element with the given style-id. This search
     * is deep and it will look past first level children.
     * 
     * @return
     */
    private String findTextFromStyleIdXX(AbstractIntroContainer parent,
            String styleId) {
        String text = doFindTextWithClassIdXXX(parent, styleId);
        if (text != null)
                return text;
        AbstractIntroContainer[] containers = (AbstractIntroContainer[]) parent
                .getChildrenOfType(AbstractIntroElement.ABSTRACT_CONTAINER);
        for (int i = 0; i < containers.length; i++) {
            text = findTextFromStyleId(containers[i], styleId);
            if (text != null)
                    return text;
        }
        return null;
    }

    /**
     * Returns the first direct child text element with the given style-id.
     * 
     * @return
     */
    private String doFindTextWithClassIdXXX(AbstractIntroContainer container,
            String styleId) {
        IntroText[] allText = (IntroText[]) container
                .getChildrenOfType(AbstractIntroElement.TEXT);
        for (int i = 0; i < allText.length; i++) {
            if (allText[i].getClassId().equals(styleId))
                    return allText[i].getText();
        }
        return null;
    }



    public boolean getShowLinkDescription() {
        String key = page.getId() + ".layout.link-description"; //$NON-NLS-1$
        String value = getProperty(key);
        if (value == null)
                value = "false"; //$NON-NLS-1$
        return value.toLowerCase().equals("true"); //$NON-NLS-1$
    }

}
