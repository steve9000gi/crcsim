package org.renci.epi.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import org.apache.log4j.Logger;
import org.renci.epi.population.PopulationService;
import org.renci.epi.util.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * Multi-action controller.
 */
@Controller
public class AppController extends BaseController {

    private final PopulationService populationService;

    private final JRCsvDataSource datasource;


    protected static Logger logger = Logger.getLogger (BaseController.class.getName ());

    @Autowired
    public AppController (PopulationService populationService) {
	this.populationService = populationService;
	InputStream input = this.getClass().getResourceAsStream ("/org/renci/epi/reports/datasource.csv");
	this.datasource = new JRCsvDataSource (input);
	this.datasource.setUseFirstRowAsHeader (true);
    }

    @RequestMapping("/")
    public String welcomeHandler () {
	populationService.getPopulation (new String [] { "0" });
	return "index";
    }

    private void dumpds () {
	InputStream in = null;
	try {
	    in = this.getClass().getResourceAsStream ("/org/renci/epi/reports/datasource.csv");
	    BufferedReader reader = new BufferedReader (new InputStreamReader (in));
	    for (String line = reader.readLine (); line != null; line = reader.readLine ()) {
		System.out.println (line);
	    }
	} catch (IOException e) {
	    e.printStackTrace ();
	} finally {
	    if (in != null) {
		try {
		    in.close ();
		} catch (IOException e) {
		    e.printStackTrace ();
		}
	    }
	}
    }

    @RequestMapping("/reports/status/{format}")
    public ModelAndView handleSimpleReportMulti (@PathVariable("format") String format,
						 HttpServletRequest request,
						 HttpServletResponse response)
	throws Exception
    {
	Map<String,Object> model = new HashMap<String,Object> ();
	model.put ("format", format);
	/**
	this.dumpds ();
	InputStream input = this.getClass().getResourceAsStream ("/org/renci/epi/reports/datasource.csv");
	JRCsvDataSource datasource = new JRCsvDataSource (input);
	datasource.setUseFirstRowAsHeader (true);
	*/
	model.put ("datasource", this.datasource);
	return new ModelAndView ("StatusReport", model);
    }

}
