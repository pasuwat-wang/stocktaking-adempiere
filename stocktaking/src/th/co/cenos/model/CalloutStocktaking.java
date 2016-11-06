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
package th.co.cenos.model;

import java.util.Properties;

import org.compiere.model.CalloutEngine;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;

/**
 * @function stocktaking
 * @package th.co.cenos.model
 * @classname CalloutStocktaking
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 6, 2016 9:08:50 PM
 */
public class CalloutStocktaking extends CalloutEngine {
	/* Clear value after */
	public String clear (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		mTab.setValue(I_Ext_StocktakingLine.COLUMNNAME_M_AttributeSetInstance_ID,null);
		mTab.setValue(I_Ext_StocktakingLine.COLUMNNAME_QtyCount,null);
		return "";
	}
}
