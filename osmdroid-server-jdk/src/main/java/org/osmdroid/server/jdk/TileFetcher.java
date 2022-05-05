/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmdroid.server.jdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;


/**
 * This is a REST web service (via Apache CXF) that provides 3 functions
 * <ul><li>getSourceList() - provides a list of human readable tile sources</li>
 * <li>getImage - gets a ZXY OSM style map tile</li>
 * <li>serves from file system, a simple open layers slippy map that makes it easy to test to see if this thing is working
 * and let's you flip tiles sources using some jquery magic</li>
 * </ul>
 *
 * @author <a href="mailto:alexoree@apache.org">Alex O'Ree</a>
 */
@Path("/")
@Produces({"image/png", "application/json", "text/html", "text/css", "text/javascript"})
@org.apache.cxf.jaxrs.model.wadl.Description("")
public class TileFetcher {

    private static final Log log = LogFactory.getLog(TileFetcher.class);
    static ObjectMapper om = new ObjectMapper();
    HashMap<String, Connection> connections = new HashMap<String, Connection>();

    public TileFetcher() throws Exception {

        initDatabases();

    }

    private void initDatabases() throws Exception {
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream("sources.properties");
        p.load(fis);
        fis.close();
        fis = null;

        Iterator<Map.Entry<Object, Object>> iterator = p.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> next = iterator.next();

            String source = (String) next.getKey();
            String filename = (String) next.getValue();
            File db = new File(filename);
            if (!db.exists()) {
                throw new FileNotFoundException("can't find the db " + filename + " current dir is " + new File(".").getAbsolutePath());
            }
            try {
                Connection conn1 = DriverManager.getConnection("jdbc:sqlite:" + filename);
                Statement stat = conn1.createStatement();
                stat.executeUpdate("CREATE TABLE IF NOT EXISTS tiles (key INTEGER PRIMARY KEY, provider TEXT, tile BLOB)");
                stat.close();
                System.out.println("adding " + source + " from file " + filename);
                connections.put(source, conn1);
            } catch (SQLException e) {
                e.printStackTrace();
                //throw new Exception("unable to initialize db", e);
            }

        }
    }

    @GET
    @Path("/sources")
    @Produces("application/json")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns a JSON string array of all available map sources")
    public String getSourceList() throws WebApplicationException, JsonProcessingException {
        System.out.println("getSourceList");
        return om.writeValueAsString(connections.keySet());
    }

    @GET//xyz no good
    //zyx no good
    //zxy closer

    @Path("/{source}/{z}/{x}/{y}.png")
    @Produces("image/png")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns png of the specific map tile from the database")
    public byte[] getImage(@PathParam("source") String id,
                           @PathParam("z") int z,
                           @PathParam("x") int x,
                           @PathParam("y") int y) throws WebApplicationException {

        Connection c = connections.get(id);
        if (c == null) {
            System.err.println(id + " isn't registered");
            throw new WebApplicationException(new Exception(id + " is not a valid tile source"), 400);
        }
        try {

            PreparedStatement prep = c.prepareStatement("Select tile from tiles where key=?;");

            long index = (((z << z) + x) << z) + y;
            System.out.println("Fetching tile " + id + z + "/" + x + "/" + y + " as " + index);
            prep.setLong(1, index);
            ResultSet executeQuery = prep.executeQuery();
            if (executeQuery.next()) {
                //Blob b= executeQuery.getBlob(1);
                //byte[] image=b.getBytes(0, (int)b.length());
                byte[] image2 = executeQuery.getBytes(1);
                //return image;
                return image2;
            }
            System.out.println(id + "Tile not found " + z + "/" + x + "/" + y + " as " + index);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

        }
        throw new WebApplicationException(404);

    }

    @GET
    @Path("/index.html")
    @Produces("text/html")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns a basic html viewer of the slippy map")
    public String getIndex() throws WebApplicationException {
        return getFile("www/openlayers.html");

    }

    @GET
    @Path("/")
    @Produces("text/html")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns a basic html viewer of the slippy map")
    public String getIndex4() throws WebApplicationException {
        return getFile("www/openlayers.html");

    }

    @GET
    @Path("/v3.5.0-dist/ol.js")
    @Produces("application/javascript")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns a basic html viewer of the slippy map")
    public String getIndex2() throws WebApplicationException {
        return getFile("www/v3.5.0-dist/ol.js");

    }

    @GET
    @Path("/v3.5.0-dist/ol.css")
    @Produces("text/css")
    @org.apache.cxf.jaxrs.model.wadl.Description("Returns a basic html viewer of the slippy map")
    public String getIndex3() throws WebApplicationException {
        return getFile("www/v3.5.0-dist/ol.css");

    }

    public static String getFile(String f) {

        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream r = new FileInputStream(f);
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
            r.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Current dir is " + new File(".").getAbsolutePath());
        }
        return sb.toString();
    }

}
