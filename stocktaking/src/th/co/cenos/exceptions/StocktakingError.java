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

/**
 * @function stocktaking
 * @package th.co.cenos.exceptions
 * @classname StockTakingError
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 9, 2016 11:11:16 AM
 */
public class StocktakingError {
	public static String ERR_CANNOT_CREATE_PI_DOC 	= "CANNOT_CREATE_PI_DOC";
	public static String ERR_CANNOT_CREATE_PI_LINE	= "CANNOT_CREATE_PI_LINE";
	public static String ERR_ONE_DOC_WAREHOUSE 		= "ONE_DOC_FOR_WAREHOUSE";
	public static String ERR_NO_WAREHOUSE_ID		= "NO_WAREHOUSE_ID";
	public static String ERR_NO_WAREHOUSE			= "NO_WAREHOUSE"; 
	public static String ERR_STOCK_FREEZING			= "STOCK_FREEZING";
	public static String ERR_CANNOT_UNFREEZE_STOCK	= "CANNOT_UNFREEZE_STOCK";
}
