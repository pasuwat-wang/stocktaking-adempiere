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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MAttributeSet;
import org.compiere.model.MDocType;
import org.compiere.model.MInventory;
import org.compiere.model.MInventoryLine;
import org.compiere.model.MInventoryLineMA;
import org.compiere.model.MPeriod;
import org.compiere.model.MWarehouse;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.process.DocumentEngine;
import org.compiere.util.DB;
import org.compiere.util.Env;

import th.co.cenos.exceptions.StocktakingError;
import th.co.cenos.exceptions.StocktakingException;

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
		
		try{
			
			createPIDoc();
		}
		catch(Exception ex){
			log.saveError("ERROR", ex);
			return DocAction.STATUS_Invalid;
		}
		
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
	private void createPIDoc() throws Exception {
		// TODO Auto-generated method stub
		MWarehouse warehouse = (MWarehouse)getM_Warehouse();
		int C_DocType_ID = MDocType.getDocType(MDocType.DOCBASETYPE_MaterialPhysicalInventory);
		
		// Create New Physical Inventory
		MInventory inventory = new MInventory(warehouse,this.get_TrxName());
		inventory.setC_DocType_ID(C_DocType_ID);
		inventory.setMovementDate(getDateTrx());
		
		if(!inventory.save(get_TrxName()))
			throw new StocktakingException(getCtx(),StocktakingError.ERR_CANNOT_CREATE_PI_DOC);
		
		createPILines(inventory);
		setM_Inventory_ID(inventory.getM_Inventory_ID());
	}
	

	/** Inventory Line				*/
	private MInventoryLine	m_line = null; 
	
	/**
	 * @param inventory
	 */
	private void createPILines(MInventory inventory) throws StocktakingException {
		// TODO Auto-generated method stub
		StringBuffer sql = new StringBuffer("SELECT s.M_Product_ID, s.M_Locator_ID, s.M_AttributeSetInstance_ID,s.QtyOnHand, p.M_AttributeSet_ID \n");
		sql.append("FROM M_Product p \n");
		sql.append("INNER JOIN M_Storage s ON (s.M_Product_ID=p.M_Product_ID) \n");
		sql.append("INNER JOIN M_Locator l ON (s.M_Locator_ID=l.M_Locator_ID) \n");
		sql.append("WHERE l.M_Warehouse_ID=? \n"); // Parameter 1 -- M_Warehouse_ID
		sql.append("AND p.IsActive='Y' AND p.IsStocked='Y' and p.ProductType='I' \n");
		sql.append("AND NOT EXISTS (SELECT * FROM M_InventoryLine il WHERE il.M_Inventory_ID=? \n"); // Parameter 2 -- M_Inventory_ID
		sql.append("\t\t\t AND il.M_Product_ID=s.M_Product_ID \n");
		
		sql.append("\t\t\t AND il.M_Locator_ID=s.M_Locator_ID \n");
		sql.append("\t\t\t AND COALESCE(il.M_AttributeSetInstance_ID,0)=COALESCE(s.M_AttributeSetInstance_ID,0)) \n");
		sql.append(" ORDER BY l.Value, p.Value, s.QtyOnHand DESC");	//	Locator/Product
		//
		int count = 0;
		PreparedStatement ppstmt = null;

		// Define Updated Stocktaking List
		List<MExtStocktakingLine> updatedL = new ArrayList<MExtStocktakingLine>();
		MExtStocktakingLine[] lines = getLines(true);
		
		try{
			ppstmt = DB.prepareStatement(sql.toString(), get_TrxName());
			ppstmt.setInt(1, this.getM_Warehouse_ID());
			ppstmt.setInt(2, inventory.getM_Inventory_ID());
			ResultSet rset = ppstmt.executeQuery();
			while(rset.next()){
				int M_Product_ID = rset.getInt(1);
				int M_Locator_ID = rset.getInt(2);
				int M_AttributeSetInstance_ID = rset.getInt(3);
				BigDecimal QtyOnHand = rset.getBigDecimal(4);
				int M_AttributeSet_ID = rset.getInt(5);
				
				BigDecimal QtyCounted = Env.ZERO;
				for(MExtStocktakingLine line : lines){
					if(line.getM_Product_ID() == M_Product_ID 
							&& line.getM_Locator_ID() == M_Locator_ID 
							&& line.getM_AttributeSetInstance_ID() == M_AttributeSetInstance_ID)
					{
						QtyCounted = line.getQtyCount();
						updatedL.add(line);
					}
				}
				
				count += createInventoryLine (inventory,M_Locator_ID, M_Product_ID, 
						M_AttributeSetInstance_ID, QtyOnHand, M_AttributeSet_ID,QtyCounted);
			}
		}
		catch(Exception ex){
			log.saveError("ERROR", ex);
			throw new StocktakingException(this.getCtx(),StocktakingError.ERR_CANNOT_CREATE_PI_LINE);
		}
		
		if(updatedL.size() < lines.length ){
			List<MExtStocktakingLine> addLineL = getStkLineToAdd(lines,updatedL);
			log.fine("Add Line :"+addLineL.size());
			for(MExtStocktakingLine addLine : addLineL){
				count += createInventoryLine (inventory,addLine.getM_Locator_ID(), addLine.getM_Product_ID(), 
						addLine.getM_AttributeSetInstance_ID(), Env.ZERO, addLine.getM_Product().getM_AttributeSet_ID(),addLine.getQtyCount());
			}
		}
		
		log.fine(String.format("%s Total Inventory Line [%s]",inventory.toString(),count));
	}
	
	/**
	 * @param lines2
	 * @param updatedL
	 * @return
	 */
	private List<MExtStocktakingLine> getStkLineToAdd(MExtStocktakingLine[] lines, List<MExtStocktakingLine> updatedL) 
	{
		List<MExtStocktakingLine> addLineL = new ArrayList<MExtStocktakingLine>();
		// TODO Auto-generated method stub
		for(MExtStocktakingLine line:lines){
			if(!updatedL.contains(line))
				addLineL.add(line);
		}
		
		return addLineL;
	}

	private int createInventoryLine (MInventory inventory , int M_Locator_ID, int M_Product_ID, 
			int M_AttributeSetInstance_ID, BigDecimal QtyOnHand, int M_AttributeSet_ID, BigDecimal QtyCount)
	{
		boolean oneLinePerASI = false;
		if (M_AttributeSet_ID != 0)
		{
			MAttributeSet mas = MAttributeSet.get(getCtx(), M_AttributeSet_ID);
			oneLinePerASI = mas.isInstanceAttribute();
		}
		if (oneLinePerASI)
		{
			MInventoryLine line = new MInventoryLine (inventory, M_Locator_ID, 
				M_Product_ID, M_AttributeSetInstance_ID, 
				QtyOnHand, QtyCount);		//	book/count
			if (line.save())
				return 1;
			return 0;
		}
			
		if (QtyOnHand.signum() == 0)
			M_AttributeSetInstance_ID = 0;
			
		if (m_line != null 
			&& m_line.getM_Locator_ID() == M_Locator_ID
			&& m_line.getM_Product_ID() == M_Product_ID)
		{
			if (QtyOnHand.signum() == 0)
				return 0;
			//	Same ASI (usually 0)
			if (m_line.getM_AttributeSetInstance_ID() == M_AttributeSetInstance_ID)
			{
				m_line.setQtyBook(m_line.getQtyBook().add(QtyOnHand));
				m_line.setQtyCount(m_line.getQtyCount().add(QtyOnHand));
				m_line.save();
				return 0;
			}
			//	Save Old Line info
			else if (m_line.getM_AttributeSetInstance_ID() != 0)
			{
				MInventoryLineMA ma = new MInventoryLineMA (m_line, 
					m_line.getM_AttributeSetInstance_ID(), m_line.getQtyBook());
				if (!ma.save())
					;
			}
			m_line.setM_AttributeSetInstance_ID(0);
			m_line.setQtyBook(m_line.getQtyBook().add(QtyOnHand));
			m_line.setQtyCount(m_line.getQtyCount().add(QtyOnHand));
			m_line.save();
			//
			MInventoryLineMA ma = new MInventoryLineMA (m_line, 
				M_AttributeSetInstance_ID, QtyOnHand);
			if (!ma.save())
				;
			return 0;
		}
			//	new line
		m_line = new MInventoryLine (inventory, M_Locator_ID, 
		M_Product_ID, M_AttributeSetInstance_ID, 
			QtyOnHand, QtyCount);		//	book/count
		if (m_line.save())
			return 1;
		return 0;
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
	
	public static boolean isWarehouseStockFreezing(MWarehouse warehouse , String trxName) throws Exception {
		boolean ret = false ;
		
		String sql = "SELECT count(1) as no_of_record FROM Ext_Stocktaking st WHERE st.M_Warehouse_ID = ? AND st.freezestock = 'Y' AND st.DocStatus NOT IN ('CO','VO','CL') ";
		PreparedStatement ppstmt = DB.prepareStatement(sql, trxName);
		ppstmt.setInt(1, warehouse.getM_Warehouse_ID());
		ResultSet rset = ppstmt.executeQuery();
		if(rset.next()){
			int no_of_record = rset.getInt(1);
			if(no_of_record > 0)
				ret = true;
		}
		
		return ret;
	}
	
	public boolean isStockFreezing(){
		return "Y".equals(getFreezeStock());
	}
}
