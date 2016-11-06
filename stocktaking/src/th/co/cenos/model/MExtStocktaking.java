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

import java.io.File;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MPeriod;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Msg;

/**
 * @function stocktaking
 * @package th.co.cenos.model
 * @classname MExtStockTaking
 * @author Pasuwat Wang (CENS ONLINE SERVICES)
 * @created Nov 2, 2016 12:23:10 PM
 */
public class MExtStocktaking extends X_Ext_Stocktaking implements DocAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String		m_processMsg = null;
	
	/**
	 * @param ctx
	 * @param Ext_StockTaking_ID
	 * @param trxName
	 */
	public MExtStocktaking(Properties ctx, int Ext_StockTaking_ID,
			String trxName) {
		super(ctx, Ext_StockTaking_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public MExtStocktaking(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean processIt(String action) throws Exception {
		m_processMsg = null;
		DocumentEngine engine = new DocumentEngine (this, getDocStatus());
		return engine.processIt (action, getDocAction());
	}

	@Override
	public boolean unlockIt() {
		log.info("unlockIt - " + toString());
		setProcessed(false);
		return true;
	}

	@Override
	public boolean invalidateIt() {
		log.info(toString());
		setDocAction(DOCACTION_Prepare);
		return true;
	}

	@Override
	public String prepareIt() {
		log.info(toString());
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
				
		if (!MPeriod.isOpen(getCtx(), getDateTrx(), 
				I_C_DocType.DOCBASETYPE_StockTaking , getAD_Org_ID()))
		{
			m_processMsg = "@PeriodClosed@";
			return DocAction.STATUS_Invalid;
		}
		
		// Should Have Line
		MExtStocktakingLine[] bLines = getLines(false);
		if(bLines.length <= 0 ){
			m_processMsg = "@NoLines@";
			return DocAction.STATUS_Invalid;
		}
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_PREPARE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		m_justPrepared = true;
		if (!DOCACTION_Complete.equals(getDocAction()))
			setDocAction(DOCACTION_Complete);
		return DocAction.STATUS_InProgress;
	}

	/**	Just Prepared Flag			*/
	private boolean		m_justPrepared = false;
	
	MExtStocktakingLine[] lines = null;
	
	public MExtStocktakingLine[] getLines(boolean query){
		if(lines == null || query)
			lines = getLines(null);
		
		return lines;
	}
	
	public MExtStocktakingLine[] getLines(String whereClause){
		String whereClauseFinal = "Ext_StockTaking_ID=? ";
		if (whereClause != null)
			whereClauseFinal += whereClause;
		
		List<MExtStocktakingLine> list = new Query(getCtx(), MExtStocktakingLine.Table_Name, whereClauseFinal, get_TrxName())
											.setParameters(new Object[]{this.getExt_Stocktaking_ID()})
											.setOrderBy(MExtStocktakingLine.COLUMNNAME_Line)
											.list();
		
		return list.toArray(new MExtStocktakingLine[list.size()]);
	}

	@Override
	public boolean approveIt() {
		log.info(toString());
		return true;
	}

	@Override
	public boolean rejectIt() {
		log.info(toString());
		//setIsApproved(false);
		return true;
	}

	@Override
	public String completeIt() {
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_BEFORE_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		log.info(toString());
		
//		Re-Check
		if (!m_justPrepared)
		{
			String status = prepareIt();
			if (!DocAction.STATUS_InProgress.equals(status))
				return status;
		}
		
		createPIDoc();
		
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this, ModelValidator.TIMING_AFTER_COMPLETE);
		if (m_processMsg != null)
			return DocAction.STATUS_Invalid;
		
		setProcessed(true);	
		//
		setDocAction(DOCACTION_Close);
		return DocAction.STATUS_Completed;
	}

	/**
	 * 
	 */
	private void createPIDoc() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean voidIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseCorrectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseAccrualIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reActivateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File createPDF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDoc_User_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		// TODO Auto-generated method stub
		return null;
	}

}
