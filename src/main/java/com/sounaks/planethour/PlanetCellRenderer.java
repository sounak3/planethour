/*
 * File: PlanetCellRenderer.java in java package com.sounaks.planethour is part of application
 * PlanetHour v1.0 - Planetary hour calculation software
 * Copyright (C) 2014 Sounak Choudhury
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contact E-mail: sounak_s@rediffmail.com
 */
package com.sounaks.planethour;

import java.awt.Color;
import java.awt.Point;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

/**
 *
 * @author Sounak Choudhury
 */
public class PlanetCellRenderer extends javax.swing.table.DefaultTableCellRenderer{
    boolean showSymbol;
    java.awt.Font font;
    java.util.ArrayList<Point> selectedCells;
    Point mouseOverCell;
    javax.swing.border.Border normalBorder, selectLeftBorder, selectRightBorder, normalCenterBorder, selectCenterBorder;
    
    public PlanetCellRenderer(boolean showSymbol, java.util.ArrayList<Point> selectedCells) {
        super();
        this.selectedCells=selectedCells;
        this.showSymbol=showSymbol;
        normalBorder = getBorder();
        selectLeftBorder = BorderFactory.createMatteBorder(1, 1, 1, 0, Color.BLUE);
        selectRightBorder = BorderFactory.createMatteBorder(1, 0, 1, 1, Color.BLUE);
        normalCenterBorder = BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Table.gridColor")==null?Color.gray:UIManager.getColor("Table.gridColor"));
        selectCenterBorder = BorderFactory.createCompoundBorder(normalCenterBorder, selectRightBorder);
        mouseOverCell = new Point(-1, -1);
    }
    
   @Override
    public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
        boolean isSelected, boolean hasFocus,
        int row, int column) {
        font = table.getFont();
        String val = value.toString();
        super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
        if(val.equalsIgnoreCase("yamag")) val = "Yamagandam";
        setText(val);
        if(column==0 || column==2) { // then set image icons for planets, else horizontal align=CENTER
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/sounaks/planethour/"+value.toString().toLowerCase()+(showSymbol?"2.png":".png"))));
            setHorizontalAlignment(SwingConstants.LEADING);
        } else {
            setIcon(null);
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        if(row==mouseOverCell.y && (mouseOverCell.x==0 || mouseOverCell.x==1)) {
            if(column==1) setBorder(selectCenterBorder);
            else if(column==0) setBorder(selectLeftBorder);
        } else if(row==mouseOverCell.y && (mouseOverCell.x==2 || mouseOverCell.x==3)) {
            if(column==3) setBorder(selectRightBorder);
            else if(column==2) setBorder(selectLeftBorder);
            else if(column==1) setBorder(normalCenterBorder);
        } else {
            if(column==1) setBorder(normalCenterBorder);
            else setBorder(normalBorder);
        }
        for(int i=0; i<selectedCells.size(); i++) {
            if(row==selectedCells.get(i).x && column==selectedCells.get(i).y) { // then make font bold and increase size by 1
                setFont(font.deriveFont(java.awt.Font.BOLD, font.getSize2D()+1));
            }
        }
        if(getFont().isBold() && !isSelected) setForeground(Color.blue); // if font=BOLD then set color=BLUE, else defaults below
        else if(isSelected) setForeground(table.getSelectionForeground());
        else if(!isSelected) setForeground(table.getForeground());
        return this;
    }

    public void setShowSymbol(boolean showSymbol) {
        this.showSymbol = showSymbol;
    }

    public void setSelectedCells(java.util.ArrayList<Point> selectedCells) {
        this.selectedCells = selectedCells;
    }

    public void setMouseOverCell(Point mouseOverCell) {
        this.mouseOverCell = mouseOverCell;
    }
}
