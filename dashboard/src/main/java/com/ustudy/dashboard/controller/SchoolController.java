package com.ustudy.dashboard.controller;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.ustudy.dashboard.model.OrgBrife;
import com.ustudy.dashboard.model.School;
import com.ustudy.dashboard.services.SchoolService;

@RestController
@RequestMapping(value="/school/")
public class SchoolController {

	private static final Logger logger = LogManager.getLogger(SchoolController.class);
	// private static final Log logger = LogFactory.getLog(SchoolController.class);
	
	@Autowired
	private SchoolService ss;
	
	@RequestMapping(value="/list/{id}", method = RequestMethod.GET)
	public List<School> getList(@PathVariable int id, HttpServletResponse resp) {
		logger.debug("endpoint /school/list/ is visited");
		List<School> res = null;
		try {
			res = ss.getList(id);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			String msg = "Failed to retrieve item list from " + id;
			try {
				resp.sendError(500, msg);
			} catch (Exception re) {
				logger.warn("Failed to set error status in response");
			}
		}
		return res;
	}
	
	@RequestMapping(value = "list/brife/{id}", method = RequestMethod.GET)
	public List<OrgBrife> getOrgBrifeList(@PathVariable int id, HttpServletResponse resp) {
		logger.debug("endpoint /school/brife/" + id + " is visited");
		List<OrgBrife> ret = null;
		try {
			ret = ss.getOrgBrifeList(id);
		} catch (Exception e) {
			logger.warn(e.getMessage());
			String msg = "getOrgBrifeList(), Failed to retrieve orgnization brife list since " + id;
			try {
				resp.sendError(500, msg);
			} catch (Exception re) {
				logger.warn("getOrgBrifeList(), Failed to set error status in response");
			}
		}
		return ret;
	}
	
	@RequestMapping(value="/add", method = RequestMethod.POST)
	public String createItem(@RequestBody @Valid School item, HttpServletResponse resp, UriComponentsBuilder builder) {
		logger.debug("endpoint /school/add/ is visited.");
		logger.debug(item.toString());
		String result = null;
		try {
		    int index = ss.createItem(item);
		    logger.debug("School created successfully with id " + index);
		    //set header location
		    resp.setHeader("Location", 
		    	builder.path("/school/view/{index}").buildAndExpand(index).toString());
		    result = "create item successfully";
		} catch (Exception e) {
			logger.warn(e.getMessage());
			result = "create item failed";
			resp.setStatus(500);
		}
		
		return result;
	}
	
	@RequestMapping(value="/update/{id}", method = RequestMethod.POST)
	public String updateItem(@RequestBody @Valid School data, @PathVariable int id, HttpServletResponse resp) {
		logger.debug("endpoint /school/update/" + id + " is visited.");
		String result = null;
		try {
			int numOfRows = ss.updateItem(data, id);
			if (numOfRows == 1)
				logger.debug("update item successfully");
			else {
				String msg = numOfRows + " items are updated, maybe something goes wrong";
				logger.warn(msg);
				throw new RuntimeException(msg);
			}
			result = "update item successfully";
		} catch (Exception e) {
			logger.warn(e.getMessage());
			result = "update item failed";
			resp.setStatus(500);
		}
		return result;
	}
	
	@RequestMapping(value="/delete/{id}", method = RequestMethod.DELETE)
	public String deleteItem(@PathVariable int id, HttpServletResponse resp) {
		logger.debug("endpoint /delete/" + id + " is visited.");
		String result = null;
		try {
			int numOfRows = ss.deleteItem(id);
			if (numOfRows != 1)
				throw new RuntimeException("Number of affected rows is " + numOfRows);
			else
				result = "item deleted";
		} catch (Exception e) {
			logger.warn(e.getMessage());
			result = "delete item failed";
			resp.setStatus(500);
		}
		return result;
	}
	
	/**
	 * To delete several items as a batch operation, list of ids should be assembled into 
	 * a list of 'ids'. Batch operation should be managed as transaction.
	 * @return
	 */
	@RequestMapping(value="/delete", method = RequestMethod.POST)
	public String delSet(@RequestBody @Valid String data, HttpServletResponse resp) {
		logger.debug("endpoint /school/delete is visited.");
		String result = null;
		try {
			int num = ss.delItemSet(data);
			if (num < 1)
				throw new RuntimeException("Number of deleted items is " + num);
			else {
				result = num + " items deleted";
			}
				
		} catch (Exception e) {
			result = "delete items failed";
			logger.warn("delSet()," + result + " --> " + e.getMessage());
			resp.setStatus(500);
		}
		return result;
	}
	
	@RequestMapping(value="/view/{id}", method = RequestMethod.GET) 
	public School displayItem(@PathVariable int id, HttpServletResponse resp) {
		logger.debug("endpoint /school/view/" + id + " is visited.");
		School item = null;
		String msg = null;
		try {
			item = ss.displayItem(id);
		} catch (IncorrectResultSizeDataAccessException ie) {
			logger.warn("displayItem(), " + ie.getLocalizedMessage());
			msg = "No items found for specified id = " + id;
		} catch (Exception e) {
			logger.warn("displayItem(), " + e.getMessage());
			msg = "Failed to retrieve item " + id;
		}
		
		if (item == null) {
			try {
				resp.sendError(500, msg);
			} catch (Exception re) {
				logger.warn("displayItem(), Failed to set error status in response");
			}
		}
				
		return item;
	}

}

