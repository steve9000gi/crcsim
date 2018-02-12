package org.renci.epi.web;

import static org.springframework.test.web.ModelAndViewAssert.*;
import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;

// Swap the default JUnit4 with the spring specific SpringJUnit4ClassRunner.
// This will allow spring to inject the application context
@RunWith(SpringJUnit4ClassRunner.class)
// Remove the MockStaticEntityMethods annotation
// Setup the configuration of the application context and the web mvc layer
@ContextConfiguration({"classpath:/spring/population-context.xml","file:src/main/webapp/WEB-INF/spring/servlet-context.xml"})
public class AppControllerTest {

    public AppControllerTest () {
    }

    @Autowired
    private ApplicationContext applicationContext;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private RequestMappingHandlerAdapter handlerAdapter;
    private AppController controller;
    
    @Before
    public void setUp () {
       request = new MockHttpServletRequest();
       response = new MockHttpServletResponse();
       
       handlerAdapter = applicationContext.getBean (RequestMappingHandlerAdapter.class);

       // I could get the controller from the context here
       controller = (AppController)applicationContext.getBean ("appController"); //new AppController (null);
    }
    
    @Test
    public void testHomePage () throws Exception {
	request.setRequestURI ("/");
	Object handler = getHandler (request);
	final ModelAndView mav = handlerAdapter.handle (request, response, handler);
	assertViewName (mav, "index");
    }

    /**
     * This method finds the handler for a given request URI. 
     * 
     * It will also ensure that the URI Parameters i.e. /context/test/{name} are added to the request
     * 
     * @param request
     * @return 
     * @throws Exception
     */
    private Object getHandler(MockHttpServletRequest request) throws Exception {
        HandlerExecutionChain chain = null;

        Map<String, HandlerMapping> map = applicationContext.getBeansOfType(HandlerMapping.class);
        Iterator<HandlerMapping> itt = map.values().iterator();

        while (itt.hasNext()) {
            HandlerMapping mapping = itt.next();
            chain = mapping.getHandler(request);
            if (chain != null) {
                break;
            }

        }
        
        if (chain == null) {
            throw new InvalidParameterException("Unable to find handler for request URI: " + request.getRequestURI());
        }
        
        return chain.getHandler ();
    }
}
