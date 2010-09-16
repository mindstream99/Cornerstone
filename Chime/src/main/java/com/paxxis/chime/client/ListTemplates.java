/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.chime.client;

/**
 * @author Robert Englander
 *
 */
public final class ListTemplates {

	public static final String LONG = 
		"<div id='rob'>{topRule}"  +
		    "<table width='100%'>" +
		    	"<tr>" + 
		    		"<td valign='top'>" + 
		    			"<div class='rob-indicator'>{name}</div>" + 
		    			"<div class='rob-msg'>{timestamp}</div>" +
		    			"<div class='rob-msg'>{types}</div>" +
		    		"</td>" +
		    		"<td valign='top'>" + 
		    			"<div class='rob-rating' align='right'>{rating}</div>" +
		    			"<div class='rob-msg' align='right'>{shortRating}</div>" +
		    			"<div class='rob-msg' align='right'>{commentCount}</div>" +
		    		"</td>" + 
		    	"</tr>" +
		    	"<tr>" + 
		    		"<td colspan='2' valign='top'>" + 
		    			"<div class='rob-msg'><br>Description:&nbsp;{desc}</div>" +
		    		"</td>" +
		    	"</tr>" +
		    "</table>" +
	    "</div>";

	public static final String SHORT = 
		"<div id='rob'>{topRule}"  +
		    "<table width='100%'>" +
		    	"<tr>" + 
		    		"<td valign='top'>" + 
		    			"<div class='rob-indicator2'>{name}</div>" + 
		    			"<div class='rob-msg'>{types}</div>" +
		    		"</td>" +
		    		"<td valign='top'>" + 
		    			"<div class='rob-rating' align='right'>{rating}</div>" +
		    			"<div class='rob-msg' align='right'>{shortRating}</div>" +
		    		"</td>" + 
		    	"</tr>" +
		    	"<tr>" + 
		    		"<td colspan='2' valign='top'>" + 
		    			"<div class='rob-msg'>Description:&nbsp;{desc}</div>" +
		    		"</td>" +
		    	"</tr>" +
		    "</table>" +
	    "</div>";
	
	public static final String TINY = 
		"<div id='rob'>{topRule}"  +
		    "<table width='100%'>" +
		    	"<tr>" + 
		    		"<td valign='top'>" + 
		    			"<div class='rob-indicator2'>{name}</div>" + 
		    		"</td>" +
		    		"<td valign='top'>" + 
		    			"<div class='rob-rating' align='right'>{rating}</div>" +
		    		"</td>" + 
		    	"</tr>" +
		    	"<tr>" + 
		    		"<td colspan='2' valign='top'>" + 
		    			"<div class='rob-msg'>{desc}</div>" +
		    		"</td>" +
		    	"</tr>" +
		    "</table>" +
	    "</div>";

	public static final String FILEGALLERY = 
		"<div id='rob'>{topRule}"  +
		    "<table width='100%'>" +
		    	"<tr>" + 
		    		"<td valign='top'>" + 
		    			"<div class='rob-indicator2'>{name}</div>" + 
		    		"</td>" +
		    		"<td valign='top'>" + 
		    			"<div class='rob-rating' align='right'>{rating}</div>" +
		    		"</td>" + 
		    	"</tr>" +
		    	"<tr>" + 
		    		"<td colspan='2' valign='top'>" + 
		    			"<div class='rob-msg'>{desc}&nbsp;&nbsp;<b>{downloadLink}</b></div>" +
		    		"</td>" +
		    	"</tr>" +
		    "</table>" +
	    "</div>";

	private ListTemplates() {
	}
}
