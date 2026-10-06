/*
 * File: DateChooser.java in java package com.sounaks.planethour is part of application
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

/**
 *
 * @author Sounak Choudhury
 */
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class DateChooser extends JComponent implements ActionListener {

    protected JButton BUTTON;
    protected JFormattedTextField TEXTFIELD;
    protected DatePane pane;
    private JWindow forPane = null;
    protected JFrame parent;
    private Component glass;
    private Point location;
    protected boolean showing;
    protected SimpleDateFormat OWN_FORMAT = new SimpleDateFormat("EEEE',' MMM dd',' yyyy ");
    private WindowHandler handler;
    private Calendar calendar;

    public DateChooser() {
        TEXTFIELD = new JFormattedTextField(OWN_FORMAT);
        TEXTFIELD.setPreferredSize(new Dimension(160, TEXTFIELD.getPreferredSize().height));
        BUTTON = new JButton(new DownArrowIcon());
        //BUTTON= new JButton("\u25BC");
        BUTTON.setActionCommand("POPUP");
        BUTTON.addActionListener(this);
        BUTTON.setPreferredSize(new Dimension(20, BUTTON.getPreferredSize().height));
        pane = new DatePane();
        pane.setBorder(BorderFactory.createLineBorder(Color.black));
        pane.addActionListener(this);
        GridBagLayout lay = new GridBagLayout();
        setLayout(lay);
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 2, 1, 0.90, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        lay.setConstraints(TEXTFIELD, gbc);
        add(TEXTFIELD);
        gbc = new GridBagConstraints(2, 0, 1, 1, 0.10, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        lay.setConstraints(BUTTON, gbc);
        add(BUTTON);
        showing = false;
        TEXTFIELD.setBorder(null);
        BUTTON.setBorder(null);
        BUTTON.setBorderPainted(false);
        setBorder(BorderFactory.createEtchedBorder());
        TEXTFIELD.setFont(BUTTON.getFont());
        TEXTFIELD.setHorizontalAlignment(JTextField.RIGHT);
        calendar = Calendar.getInstance();
    }

    public void setBackground(Color color, boolean paintButton) {
        Component comps[] = getComponents();
        for (int i = 0; i < comps.length; i++) {
            if ((comps[i] instanceof JButton) && paintButton) {
                comps[i].setBackground(color);
            } else if (comps[i] instanceof JTextField) {
                ((JTextField) comps[i]).setBackground(color);
            }
        }
    }

    public Date getDate() {
        Date temp = new Date();
        String tmp = OWN_FORMAT.format(temp);
        try {
            tmp = TEXTFIELD.getText();
            temp = OWN_FORMAT.parse(tmp, new ParsePosition(0));
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int min = calendar.get(Calendar.MINUTE);
            int sec = calendar.get(Calendar.SECOND);
            calendar.setTime(temp);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.SECOND, sec);
        } catch (NullPointerException ne) {
            TEXTFIELD.setText(OWN_FORMAT.format(temp));
        }
        return temp == null ? new Date() : calendar.getTime();
    }

    public void setDate(Date date) //Assuring that input=Date() object.
    {								//And no parse exception will be thrown.
        try {
            TEXTFIELD.setText(OWN_FORMAT.format(date));
            TEXTFIELD.commitEdit();
            calendar.setTime(date);
        } catch (ParseException pse) {
        }
    }

    public SimpleDateFormat getFormat() {
        return OWN_FORMAT;
    }

    protected void showHide(boolean show) {
        if (show && forPane == null) {
            pane.setSelectedDate(getDate());
            parent = (JFrame) SwingUtilities.windowForComponent(this);
            forPane = new JWindow(parent);
            forPane.add(pane);
            forPane.pack();
            setCurLocation(this, forPane);
            forPane.setVisible(show);
            forPane.setFocusCycleRoot(true);
            glass = parent.getGlassPane();
            if (glass != null) {
                glass.setVisible(show);
            }
            handler = new WindowHandler();
            glass.addMouseListener(handler);
            addAncestorListener(handler);
            parent.addWindowListener(handler);
            parent.addWindowStateListener(handler);
        } else {
            try {
                showing = false;
                forPane.setVisible(false);
                forPane.dispose();
                forPane = null;
                glass.removeMouseListener(handler);
                glass.setVisible(show);
                parent.setFocusCycleRoot(true);
                BUTTON.requestFocusInWindow();
                BUTTON.requestFocus();
                removeAncestorListener(handler);
                parent.removeWindowListener(handler);
                parent.removeWindowStateListener(handler);
                handler = null;
            } catch (Exception rme) {
                System.out.println("Already removed.");
            }
        }
    }

    protected void setCurLocation(Component master, Component pop) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        location = new Point();
        location = TEXTFIELD.getLocation(location);
        SwingUtilities.convertPointToScreen(location, this);
        if (location.y + pop.getHeight() + TEXTFIELD.getHeight() > screen.getHeight()) {
            pop.setLocation(location.x, location.y - pop.getHeight());
        } else {
            pop.setLocation(location.x, location.y + TEXTFIELD.getHeight());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        BUTTON.setEnabled(enabled);
        TEXTFIELD.setEnabled(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals("POPUP")) {
            showing = !showing;
            showHide(showing);
        }
        if (pane.getInt(ae.getActionCommand()) > 0) {
            Date date = pane.getSelectedDate();
            TEXTFIELD.setText(OWN_FORMAT.format(date));
            showHide(false);
        }
    }

    class WindowHandler extends WindowAdapter implements AncestorListener, MouseListener {

        @Override
        public void windowIconified(WindowEvent we) {
            showHide(false);
        }

        @Override
        public void windowStateChanged(WindowEvent we) {
            showHide(false);
        }

        @Override
        public void windowDeactivated(WindowEvent we) {
            showHide(false);
        }

        @Override
        public void ancestorMoved(AncestorEvent ace) {
            showHide(false);
        }

        @Override
        public void ancestorAdded(AncestorEvent ace) {
        }

        @Override
        public void ancestorRemoved(AncestorEvent ace) {
        }

        @Override
        public void mousePressed(MouseEvent me) {
            showHide(false);
        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseClicked(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }
    }
}