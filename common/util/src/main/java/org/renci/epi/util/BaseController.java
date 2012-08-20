package org.renci.epi.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.context.WebApplicationContext;

/**
 * Annotation-driven <em>MultiActionController</em>
 */
@Controller
public class BaseController {

    private Object service;

    protected Object getService (String serviceName) {
	return this.service;
    }

}
