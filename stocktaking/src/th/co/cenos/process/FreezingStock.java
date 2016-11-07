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
package th.co.cenos.process;

import java.util.logging.Level;

import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.CLogger;

import th.co.cenos.model.MExtStocktaking;

/**
 * @function stocktaking
 * @package th.co.cenos.process
 * @classname FreezingStock
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 2, 2016 11:21:33 AM
 */
public class FreezingStock extends SvrProcess {
	
	private static CLogger log = CLogger.getCLogger(FreezingStock.class);

	int RECORD_ID = 0;
	
	boolean isEnabled = false;
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		log.fine("FreezingStock.prepare() ID "+getRecord_ID());
		RECORD_ID = getRecord_ID();
		
		
		log.fine("Get Parameters");
		ProcessInfoParameter[] para = getParameter();
		for (int i = 0; i < para.length; i++)
		{
			String name = para[i].getParameterName();
			if("FreezeStock".equals(name))
				isEnabled = para[i].getParameterAsBoolean();
			else if (para[i].getParameter() == null)
				;			
			else
				log.log(Level.SEVERE, "Unknown Parameter: " + name);
		}
	}

	@Override
	protected String doIt() throws Exception {
		if(RECORD_ID == 0)
			return "No record id!";
		
		log.fine("Find Stocktaking Record "+RECORD_ID);
		MExtStocktaking stocktaking = new MExtStocktaking(this.getCtx(),RECORD_ID,get_TrxName());
		
		log.fine("Set Enabled Stocktaking : "+isEnabled);
		
		if(isEnabled)
			stocktaking.setFreezeStock("Y");
		else
			stocktaking.setFreezeStock("N");
		
		log.fine("Stocktaking Save");
		if(!stocktaking.save(get_TrxName())){
			
		}

		return String.format("Warehouse %s was %s",stocktaking.getM_Warehouse().getName(),(isEnabled?"freezed":"unfreezed"));
	}

}
