/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package th.co.cenos.exceptions;

import java.util.Properties;

import org.compiere.util.Msg;

/**
 * @function stocktaking
 * @package th.co.cenos.exceptions
 * @classname StocktakingException
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 9, 2016 11:07:26 AM
 */
public class StocktakingException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Properties _ctx;
	private String _error;
	
	public StocktakingException(Properties ctx, String error){
		_ctx = ctx;
		_error = error;
	}

	public String getMessage() {
		// TODO Auto-generated method stub
		return Msg.getMsg(_ctx, _error);
	}
	
}
