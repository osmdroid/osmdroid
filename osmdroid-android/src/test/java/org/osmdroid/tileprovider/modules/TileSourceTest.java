package org.osmdroid.tileprovider.modules;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.osmdroid.tileprovider.tilesource.bing.ImageryMetaData;
import org.osmdroid.tileprovider.tilesource.bing.ImageryMetaDataResource;

/**
 * created on 7/19/2019.
 *
 * @author Alex O'Ree
 */
public class TileSourceTest {

    @Test
    @Ignore
    public void bingTest() throws Exception {

        JSONObject obj = new JSONObject("{\n" +
            "    \"glossary\": {\n" +
            "        \"title\": \"example glossary\",\n" +
            "\t\t\"GlossDiv\": {\n" +
            "            \"title\": \"S\",\n" +
            "\t\t\t\"GlossList\": {\n" +
            "                \"GlossEntry\": {\n" +
            "                    \"ID\": \"SGML\",\n" +
            "\t\t\t\t\t\"SortAs\": \"SGML\",\n" +
            "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" +
            "\t\t\t\t\t\"Acronym\": \"SGML\",\n" +
            "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" +
            "\t\t\t\t\t\"GlossDef\": {\n" +
            "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" +
            "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" +
            "                    },\n" +
            "\t\t\t\t\t\"GlossSee\": \"markup\"\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}");
        JSONObject jsonObj  = new JSONObject( "{\"a\":\"1\",\"b\":null}");

        String validResponse="{\"authenticationResultCode\":\"ValidCredentials\",\"brandLogoUri\":\"" +
            "http:\\/\\/dev.virtualearth.net\\/Branding\\/logo_powered_by.png\",\"copyright\":\"" +
            "Copyright © 2019 Microsoft and its suppliers. All rights reserved. This API cannot be " +
            "accessed and the content and any results may not be used, reproduced or transmitted in " +
            "any manner without express written permission from Microsoft Corporation.\",\"" +
            "resourceSets\":[{\"estimatedTotal\":1,\"resources\":[{\"__type\":\"" +
            "ImageryMetadata:http:\\/\\/schemas.microsoft.com\\/search\\/local\\/ws\\/rest\\/v1\",\"imageHeight\":256,\"" +
            "imageUrl\":\"http:\\/\\/ecn.{subdomain}.tiles.virtualearth.net\\/tiles\\/r{quadkey}.jpeg?g=7283&mkt={culture}&shading=hill\",\"" +
            "imageUrlSubdomains\":[\"t0\",\"t1\",\"t2\",\"t3\"],\"imageWidth\":256,\"imageryProviders\":null,\"vintageEnd\":null,\"" +
            "vintageStart\":null,\"zoomMax\":21,\"zoomMin\":1}]}],\"statusCode\":200,\"statusDescription\":\"OK\",\"traceId\":\"9edcce1e9ce1458eb683e86e2320f2cf|BN000021CB|7.7.0.0\"}"
            ;
        //as of 7/19/2019
        ImageryMetaDataResource instanceFromJSON = ImageryMetaData.getInstanceFromJSON(validResponse);
        Assert.assertNotNull(instanceFromJSON);
        Assert.assertNotNull(instanceFromJSON.copyright);
        Assert.assertNotNull(instanceFromJSON.getSubDomain());

    }
}
