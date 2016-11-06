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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.compiere.model.MClient;
import org.compiere.model.MInvoice;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Msg;

/**
 * @function stocktaking
 * @package th.co.cenos.model
 * @classname StocktakingValidator
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 6, 2016 10:16:36 PM
 */
public class StocktakingValidator implements ModelValidator {
	
	/** Logger */
	private static CLogger log = CLogger.getCLogger(StocktakingValidator.class);
	/** Client */
	private int m_AD_Client_ID = -1;
	
	// Context Properties
	private Properties m_ctx;
	
	// Error Message
	private String message = "";

	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		// TODO Auto-generated method stub
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			log.info(client.toString());
		} else {
			log.info("Initializing global validator: " + this.toString());
		}
		
		/// Add DocValidator in Invoice Document
		engine.addModelChange(MExtStocktaking.Table_Name, this);
	}

	@Override
	public int getAD_Client_ID() {
		return m_AD_Client_ID;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		log.info("AD_User_ID=" + AD_User_ID);
		
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		log.info("Model Change "+po.get_TableName() + " Type: " + type);
		m_ctx = po.getCtx();
		
		if(MExtStocktaking.Table_Name.equals(po.get_TableName())){
			MExtStocktaking stocktaking = (MExtStocktaking)po;
			if(hasNotCompletedStocktakingForWarehouse(stocktaking)){ // Check Invoice Customer 
				return getMsg(ERR_ONE_DOC_WAREHOUSE);
			}
		}
		
		return null;
	}
	
	private String getMsg(String AD_Message){
		return Msg.getMsg(m_ctx, AD_Message);
	}
	
	private String ERR_ONE_DOC_WAREHOUSE = "ONE_DOC_FOR_WAREHOUSE"; 

	/**
	 * @param stocktaking
	 * @return
	 * @throws SQLException 
	 */
	private boolean hasNotCompletedStocktakingForWarehouse(MExtStocktaking stocktaking) throws SQLException {
		String sql = "SELECT count(1) FROM Ext_Stocktaking st WHERE st.M_Warehouse_ID = ? AND st.DocStatus NOT IN ('CO','VO') AND st.Ext_Stocktaking_ID <> ? ";
		
		PreparedStatement ppstmt = DB.prepareStatement(sql, stocktaking.get_TrxName());
		ppstmt.setInt(1, stocktaking.getM_Warehouse_ID());
		ppstmt.setInt(2, stocktaking.getExt_Stocktaking_ID());

		ResultSet rset = ppstmt.executeQuery();
		if(rset.next()){
			int no_of_records = rset.getInt(1);
			if(no_of_records > 0)
				return true;
		}
		
		DB.close(rset);
		DB.close(ppstmt);
		
		return false;
	}

	public String docValidate(PO po, int timing) {
		// TODO Auto-generated method stub
		return null;
	}

}
