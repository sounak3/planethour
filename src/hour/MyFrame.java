/*
 * File: MyFrame.java in java package hour is part of application
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
package hour;
import java.awt.Color;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.event.MouseAdapter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Sounak Choudhury
 *
 * The main class for Planetary Hour application
 */
public class MyFrame extends javax.swing.JFrame implements Runnable {
    private boolean debug=false;
    /**
     *
     * Creates new form MyFrame, initializes database, components, default values and starts updater thread
     *
     */
    public MyFrame() {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            con = java.sql.DriverManager.getConnection("jdbc:derby:cities.db;create=true");
            ps = con.prepareStatement("select * from APP.CITY where Country like 'Desktop' and City like 'Desktop'");
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                X = Integer.parseInt(rs.getString("Lat"));
                Y = Integer.parseInt(rs.getString("Lon"));
                blackbg = rs.getString("NoSo").equalsIgnoreCase("S");
                symbolOn = rs.getString("EaWe").equalsIgnoreCase("W");
                ampmTime = rs.getString("GMTDiff").startsWith("T");
            }
            rs.close();
            ps.close();
        } catch (ClassNotFoundException cnfe) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, "The Specified Driver Does not Exist...", cnfe);
        } catch (SQLException sqle) {
            if (sqle.getErrorCode() == 0) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, "No Suitable Driver Found...", sqle);
            } else if (sqle.getErrorCode() == 1017) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, "Wrong UserName Or Password...", sqle);
            } else if (sqle.getErrorCode() == 1034) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, "Database not Started...", sqle);
            }
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, sqle.getErrorCode() + ", " + sqle.getSQLState(), sqle);
        } catch (NumberFormatException nfe) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, "Error parsing window location from db...", nfe);
        } finally {
            selectedCity = getSelectedPlaceRecord();
        }
        cal = Calendar.getInstance(selectedCity.getTimezone());
        sysTime = curTime = System.currentTimeMillis();
        planets = new Planet[24];
        spPlanet = new Planet[6];
        allPlanets = new Planet[30];
        selectedDate = new Date();
        selectedCells = new ArrayList<Point>();
        selectedRow=0;
        selectedColumn=0;
        tableBGColor= Color.getColor("Table.background", Color.white);
        panelBGColor=getBackground();
        changeBackground(false);
        initComponents();
        cellRenderer = new PlanetCellRenderer(symbolOn, selectedCells);
        setLocation(X, Y);
        fillPlanetsArray(selectedCity, selectedDate);
        updateMainForm(selectedCity);
        showHideSymbols(selectedCells);
        resizeColumns();
        threadStart();
        initTray();
    }
    
    java.sql.Connection con;
    PreparedStatement ps;
    int X=0, Y=0;
    boolean blackbg=false;
    boolean symbolOn=false;
    final String formTitle = "PlanetHour";
    private Object[] countryNames;
    private javax.swing.DefaultComboBoxModel cityNames;
    private PlaceRecord selectedCity;
    private String selectedCountry;
    private boolean formIsUpdating = false;
    private String formUpdateSource = "none";
    private Date selectedDate;
    private int selectedRow;
    private int selectedColumn;
    private Planet planets[], spPlanet[], allPlanets[], thl, nhl, spl, nextDay1stSpPlanet, nextDay1stPlanet;
    private Calendar cal;
    private long curTime;
    private long sysTime;
    private Thread oneSecThread;
    private boolean running = false;
    private boolean showTable = true;
    private boolean ampmTime = false;
    private boolean selectionIsChanging = false;
    static final Color tableContrastBGColor = Color.black;
    static final Color tableContrastFGColor = Color.white;
    static final Color panelContrastBGColor = Color.black;
    static final Color panelContrastFGColor = Color.white;
    Color tableBGColor = Color.white;
    static final Color tableFGColor = Color.black;
    Color panelBGColor = Color.white;
    static final Color panelFGColor = Color.black;
    private Color panelForeground;
    private Color tableForeground;
    private Color panelBackground;
    private Color tableBackground;
    private java.util.ArrayList<Point> selectedCells;
    private java.awt.Rectangle currBounds;
    private Point currCell;
    private SystemTray tray;
    private java.awt.TrayIcon trayIcon;
    private PlanetCellRenderer cellRenderer;
    private String aboutString = "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by "+
        "the Free Software Foundation, either version 3 of the License, or any later version.   "+
        "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of "+
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.   "+
        "You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.";

    /**
     * Get the panel foreground color
     * @return the value of panelForeground
     */
    public Color getPanelForeground() {
        return panelForeground==null?panelFGColor:panelForeground;
    }

    /**
     * Get the panel background color
     * @return the value of panelBackground
     */
    public Color getPanelBackground() {
        return panelBackground==null?panelBGColor:panelBackground;
    }

    /**
     * Get the table foreground color
     * @return the value of tableForeground
     */
    public Color getTableForeground() {
        return tableForeground==null?tableFGColor:tableForeground;
    }

    /**
     * Get the table background color
     * @return the value of tableBackground
     */
    public Color getTableBackground() {
        return tableBackground==null?tableBGColor:tableBackground;
    }

    /**
     * Stop the thread started using threadStart() to perform calculations every minute or as required
     */
    public final void threadStop() {
        running = false;
        try {
            oneSecThread.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        thl = null;
        nhl = null;
        spl = null;
    }
    
    /**
     * Start the thread to perform calculations every minute or as required
     */
    public final void threadStart() {
        oneSecThread = new Thread(this, "OneSecThread");
        running = true;
        oneSecThread.start();
    }

    /**
     * Get the value of selectedDate as a Date object
     * @return the value of selectedDate
     */
    public Date getSelectedDate() {
        return selectedDate;
    }

    /**
     * Set the value of selectedDate
     * @param selectedDate new value of selectedDate
     */
    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    /**
     * Get the value of selectedCountry as String
     * @return the value of selectedCountry as String
     */
    public String getSelectedCountry() {
        return selectedCountry;
    }

    /**
     * Get the value of selectedPlaceRecord as a PlaceRecord
     * @return the value of selectedPlaceRecord as a PlaceRecord
     */
    public final PlaceRecord getSelectedPlaceRecord() {
        int i = 0;
        PlaceRecord firstRecord, selectedKey;
        selectedKey = PlaceRecord.capital();
        try {
            // Get the 1st selected record
            ps = con.prepareStatement("select * from APP.CITY where \"KEY\" = TRUE");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (i == 0) {
                    selectedKey = new PlaceRecord(rs.getString("City"),
                            rs.getString("Lat"), rs.getString("NoSo").equalsIgnoreCase("N") ? true : false,
                            rs.getString("Lon"), rs.getString("EaWe").equalsIgnoreCase("E") ? true : false,
                            rs.getString("GMTDiff"));
                    selectedCountry = rs.getString("Country");
                }
                i++;
            }
            rs.close();
            ps.close();
            // Unselect other selected records
            if (i > 1) {
                ps = con.prepareStatement("update APP.City set \"KEY\" = false where Key like true");
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("update APP.City set \"KEY\" = true where City like '" + selectedKey.place_name + "'");
                ps.executeUpdate();
                ps.close();
            }
            // Get the 1st record, in case no selected record exists
            ps = con.prepareStatement("select * from APP.City");
            rs = ps.executeQuery();
            rs.next();
            firstRecord = new PlaceRecord(rs.getString("City"),
                    rs.getString("Lat"), rs.getString("NoSo").equalsIgnoreCase("N") ? true : false,
                    rs.getString("Lon"), rs.getString("EaWe").equalsIgnoreCase("E") ? true : false,
                    rs.getString("GMTDiff"));
            if (i == 0) {
                selectedKey = firstRecord;
                selectedCountry = rs.getString("Country");
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        return selectedKey;
    }

    /**
     * Adds a city into the database
     * @param country country of the city to be added
     * @param city name of the city
     * @param lat latitude of the city. This is a 5 digit number (3 digit degrees and 2 digit minutes concatenated) as string
     * @param lon longitude of the city. This is a 5 digit number (3 digit degrees and 2 digit minutes concatenated) as string
     * @param noso latitude hemisphere of the city. Either N (for north) or S (for south)
     * @param eawe longitude direction of the city from prime meridian. Either E (for east) or W (for west)
     * @param tz time zone of the city
     * @return error message if any error occurred during adding city OR blank string if successfully executed
     */
    public final String addCity(String country, String city, String lat, String lon, String noso, String eawe, String tz) {
        boolean recordExists;
        int usrInput;
        try {
            PreparedStatement ps1 = con.prepareStatement("select * from APP.City where Country like '" + jComboBox1.getSelectedItem().toString()
                    + "' and City like '" + jComboBox2.getSelectedItem().toString() + "'");
            ResultSet rs = ps1.executeQuery();
            recordExists = rs.next();
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            return "Error occurred while adding city " + city + ": " + ex.getMessage();
        }
        if (recordExists) {
            String alertMsg = "The specified City and Country combination already exists. Click Yes to replace, No to discard changes and Cancel to re-edit?";
            usrInput = JOptionPane.showConfirmDialog(this, alertMsg);
            switch (usrInput) {
                case JOptionPane.YES_OPTION:
                    try {
                        ps = con.prepareStatement("update APP.City set Lat='" + lat + "', Lon='" + lon + "', NoSo='" + noso + "', EaWe='" + eawe + "', GMTDiff='" + tz + "', \"KEY\"=false where Country like '" + country + "' and City like '" + city + "'");
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
                        return "Error occurred while adding city " + city + ": " + ex.getMessage();
                    }
                    break;
                case JOptionPane.NO_OPTION:
                    if (formUpdateSource.equalsIgnoreCase("none")) {
                        formUpdateSource = "updateNoOption";
                    }
                    if (formUpdateSource.equalsIgnoreCase("updateNoOption")) {
                        jComboBox1.setSelectedItem(getSelectedCountry());
                        getCityNames(jComboBox1.getModel().getSelectedItem().toString());
                        jComboBox2.setSelectedItem(getSelectedCity());
                        updateSettingsForm(getSelectedCountry(), getSelectedCity());
                    }
                    return "";
                case JOptionPane.CANCEL_OPTION:
                    return "";
            }
        } else {
            try {
                ps = con.prepareStatement("insert into City values ('" + city + "', '" + lat + "', '" + lon + "', '" + noso + "', '" + eawe + "', '" + country + "', '" + tz + "', " + false + ")");
                ps.executeUpdate();
                ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
                return "Error occurred while adding city " + city + ": " + ex.getMessage();
            }
        }
        return "City " + city + " added successfully.";
    }

    /**
     * Removes a city from the database
     * @param country name of the country to which the city belongs
     * @param city name of the city
     * @return error message if any occurred during removing city OR blank string of executed successfully
     */
    public final String removeCity(String country, String city) {
        boolean recordExists;
        int usrInput;
        try {
            PreparedStatement ps1 = con.prepareStatement("select * from APP.City where Country like '" + jComboBox1.getSelectedItem().toString()
                    + "' and City like '" + jComboBox2.getSelectedItem().toString() + "'");
            ResultSet rs = ps1.executeQuery();
            recordExists = rs.next();
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            return "Error occurred while deleting city " + city + ": " + ex.getMessage();
        }
        if (recordExists) {
            String alertMsg = "This will permanently delete City " + city + " from database. Continue?";
            usrInput = JOptionPane.showConfirmDialog(this, alertMsg, "Confirm delete action", JOptionPane.YES_NO_OPTION);
            switch (usrInput) {
                case JOptionPane.YES_OPTION:
                    try {
                        ps = con.prepareStatement("delete from City where Country like '" + country + "' and City like '" + city + "'");
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
                        return "Error occurred while deleting city " + city + ": " + ex.getMessage();
                    }
                    break;
                case JOptionPane.NO_OPTION:
                    return "";
            }
        } else {
            return "Error occurred while deleting. City " + city + " does not exists in database.";
        }
        return "City " + city + " deleted successfully.";
    }

    /**
     * Set the value of selectedPlaceRecord based on country and city given
     * @param country the given country
     * @param city the given city to select
     * @return true if operation completes successfully, else false
     */
    public boolean setSelectedPlaceRecord(String country, String city) {
        boolean recordExists=false;
        if(country==null || city==null) throw new NullPointerException("Either country name or city name is null.");
        else { // verify if the given city and country combination exists
            try {
                ps = con.prepareStatement("select * from APP.City where Country like '" + country
                        + "' and City like '" + city + "'");
                ResultSet rs = ps.executeQuery();
                recordExists = rs.next();
                if(recordExists) {
                    selectedCity = new PlaceRecord(rs.getString("City"),
                            rs.getString("Lat"), rs.getString("NoSo").equalsIgnoreCase("N") ? true : false,
                            rs.getString("Lon"), rs.getString("EaWe").equalsIgnoreCase("E") ? true : false,
                            rs.getString("GMTDiff"));
                    selectedCountry = rs.getString("Country");
                    rs.close();
                    ps.close();

                    ps = con.prepareStatement("update APP.City set \"KEY\" = false where \"KEY\" = true"); // unselect all records
                    ps.executeUpdate();
                    ps.close();
                    
                    ps = con.prepareStatement("update APP.City set \"KEY\" = true where Country like '" + country+ "' and City like '" + city + "'");
                    ps.executeUpdate();
                    ps.close();
                    return true;
                } else {
                    rs.close();
                    ps.close();
                    return false;
                }
            } catch (SQLException ex) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
    }

    /**
     * Get the value of current selectedCity
     * @return the value of selectedCity
     */
    public String getSelectedCity() {
        return selectedCity.place_name;
    }

    /**
     * Gets all the cities in the specified country as a ComboBoxModel to be used
     * in a JComboBox
     * @param country the specified country name
     * @return a ComboBoxModel containing all the city names in the country
     */
    public javax.swing.ComboBoxModel getCityNames(String country) {
        if (cityNames == null) {
            cityNames = new javax.swing.DefaultComboBoxModel();
        } else {
            cityNames.removeAllElements();
        }
        try {
            ps = con.prepareStatement("select City from APP.City where Country = '" + country + "'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cityNames.addElement(rs.getString("City"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (cityNames.getSize() == 0) {
                cityNames.addElement("No Records Found");
            }
        }
        return cityNames;
    }

    /**
     * Get the value of all countryNames
     * @return the value of countryNames
     */
    Object[] getCountryNames() {
        if (countryNames == null) {
            ArrayList<String> als1 = new ArrayList<String>();
            try {
                ps = con.prepareStatement("select Name from APP.Country");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    als1.add(rs.getString("Name"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (als1.isEmpty()) {
                    als1.add("No Records Found");
                }
            }
            countryNames = als1.toArray();
        }
        return countryNames;
    }

    /**
     * Get the value of countryNames at specified index
     * @param index of the country in the list/array
     * @return the value of countryNames at specified index
     */
    public Object getCountryNames(int index) {
        return this.countryNames[index];
    }

    /**
     * Method to update settings form with the selected PlaceRecord and Date values
     * based on the country and city name given
     * @param countryName country name from database
     * @param cityName city name which must be in the given country specified in countryName
     */
    public final void updateSettingsForm(String countryName, String cityName) {
        if (!formIsUpdating) {
            formIsUpdating = true;
            PlaceRecord firstRecord = new PlaceRecord();
            PreparedStatement ps1;
            ResultSet rs;
            try {
                ps1 = con.prepareStatement("select * from APP.City where Country like '" + countryName + "' and City like '" + cityName + "'");
                rs = ps1.executeQuery();
                if (rs.next()) {
                    firstRecord = new PlaceRecord(rs.getString("City"),
                            rs.getString("Lat"), rs.getString("NoSo").equalsIgnoreCase("N") ? true : false,
                            rs.getString("Lon"), rs.getString("EaWe").equalsIgnoreCase("E") ? true : false,
                            rs.getString("GMTDiff"));
                }
                rs.close();
                ps1.close();
            } catch (SQLException ex) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            String temp[] = PlaceRecord.toDegreeMinute(firstRecord.getDecimalLatitude());
            jTextField3.setText(temp[0].startsWith("-") ? fillZeroInText(temp[0].substring(1), 3) : fillZeroInText(temp[0], 3));
            jTextField4.setText(fillZeroInText(temp[1], 2));
            jComboBox3.setSelectedIndex(temp[0].startsWith("-") ? 1 : 0);
            String tmp[] = PlaceRecord.toDegreeMinute(firstRecord.getDecimalLongitude());
            jTextField1.setText(tmp[0].startsWith("-") ? fillZeroInText(tmp[0].substring(1), 3) : fillZeroInText(tmp[0], 3));
            jTextField2.setText(fillZeroInText(tmp[1], 2));
            jComboBox4.setSelectedIndex(tmp[0].startsWith("-") ? 1 : 0);
            if (firstRecord.toString().equals(new PlaceRecord().toString())) {
                try {
                    ps1 = con.prepareStatement("select GMTDIF from APP.Country where Name like '" + countryName + "'");
                    rs = ps1.executeQuery();
                    if (rs.next()) {
                        jTextField5.setText(rs.getString("GMTDIF").replace('.', ':'));
                    }
                    rs.close();
                    ps1.close();
                } catch (SQLException ex) {
                    Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                jTextField5.setText(firstRecord.getTimezone().getID().substring(3));
            }
            dateChooser1.setDate(selectedDate);
            formIsUpdating = false;
        }
        formUpdateSource = "none";
    }

    /**
     * Method to verify inputs in the settings form.
     * @return A null value if all input constraints are OK, else a <code>String</code> indicating the error as message
     */
    private String verifyInputs() {
        String alertMessage = null;
        String emptyStr = "";
        String invalidCharsForCountry = PlaceRecord.getInvalidChars(jComboBox1.getSelectedItem().toString(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,.-&()[]{}/<>1234567890 ");
        String invalidCharsForCity = PlaceRecord.getInvalidChars(jComboBox2.getSelectedItem().toString(), "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz,.-&()[]{}/<>1234567890 ");
        String invalidLatitudeCause = PlaceRecord.checkLatitudeError(jTextField3.getText(), jTextField4.getText());
        String invalidLongitudeCause = PlaceRecord.checkLongitudeError(jTextField1.getText(), jTextField2.getText());
        String invalidTimezoneCause = PlaceRecord.checkTimezoneError(jTextField5.getText());
        if (jComboBox1.getSelectedItem().toString().equals(emptyStr)) {
            alertMessage = "Country name cannot be left blank.";
        } else if (jComboBox2.getSelectedItem().toString().equals(emptyStr)) {
            alertMessage = "City name cannot be left blank.";
        } else if (invalidCharsForCountry != null) {
            alertMessage = "Invalid characters in Country name: " + invalidCharsForCountry;
        } else if (invalidCharsForCity != null) {
            alertMessage = "Invalid characters in City name: " + invalidCharsForCity;
        } else if (invalidLatitudeCause != null) {
            alertMessage = invalidLatitudeCause;
        } else if (invalidLongitudeCause != null) {
            alertMessage = invalidLongitudeCause;
        } else if (invalidTimezoneCause != null) {
            alertMessage = invalidTimezoneCause;
        }
        return alertMessage;
    }

    /**
     *
     * This method is called from within the constructor to initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is always
     *
     * regenerated by the Form Editor.
     *
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        popupMenu1 = new java.awt.PopupMenu();
        menuItem1 = new java.awt.MenuItem();
        menuItem2 = new java.awt.MenuItem();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jCheckBox3 = new javax.swing.JCheckBox();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jCheckBox2 = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jComboBox4 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        dateChooser1 = new hour.DateChooser();
        jButton4 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel5 = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 0), new java.awt.Dimension(30, 32767));
        jPanel9 = new javax.swing.JPanel();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        jLabel8 = new javax.swing.JLabel();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        jLabel16 = new javax.swing.JLabel();
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        jLabel14 = new javax.swing.JLabel();

        popupMenu1.setLabel("popupMenu1");

        menuItem1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        menuItem1.setLabel("Show");
        menuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItem1ActionPerformed(evt);
            }
        });
        popupMenu1.add(menuItem1);
        popupMenu1.addSeparator();
        menuItem2.setLabel("Quit");
        menuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItem2ActionPerformed(evt);
            }
        });
        popupMenu1.add(menuItem2);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(formTitle);
        setIconImage(Planet.getEarthImage(getClass()));
        setLocationByPlatform(X==0 && Y==0);
        setPreferredSize(new java.awt.Dimension(465, 490));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setPreferredSize(new java.awt.Dimension(300, 455));
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jPanel1.setPreferredSize(new java.awt.Dimension(300, 300));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", 2, 0));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(452, 440));

        jTable1.setBackground(getTableBackground());
        jTable1.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jTable1.setForeground(getTableForeground());
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Planets During", "Day Time", "Planets During", "Night Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setCellSelectionEnabled(true);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                jTable1MouseExited(evt);
            }
        });
        jTable1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                jTable1MouseMoved(evt);
            }
        });
        jTable1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTable1FocusLost(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTable1KeyTyped(evt);
            }
        });
        jTable1.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            int tempR=0;
            public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
                if(!lse.getValueIsAdjusting() && !selectionIsChanging) {
                    selectionIsChanging = true;
                    selectedRow = lse.getLastIndex()==tempR?lse.getFirstIndex():lse.getLastIndex();
                    tempR = selectedRow;
                    selectedColumn = jTable1.getSelectedColumn()==-1?selectedColumn:jTable1.getSelectedColumn();
                    //System.out.println(selectedColumn);
                    if(jTable1.hasFocus() && selectedColumn==-1) {
                        jTable1.changeSelection(selectedRow, 0, true, false);
                        jTable1.changeSelection(selectedRow, 1, true, true);
                    }
                    else if(selectedColumn==0 || selectedColumn==2) jTable1.changeSelection(selectedRow, selectedColumn+1, true, true);
                    else jTable1.changeSelection(selectedRow, selectedColumn-1, true, true);
                    selectionIsChanging = false;
                }
            }
        });
        jTable1.getColumnModel().getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            int tempC=0;
            public void valueChanged(javax.swing.event.ListSelectionEvent lse) {
                if(!lse.getValueIsAdjusting() && !selectionIsChanging) {
                    selectionIsChanging = true;
                    selectedRow = jTable1.getSelectedRow();
                    selectedColumn = lse.getLastIndex()==tempC?lse.getFirstIndex():lse.getLastIndex();
                    //            System.out.print(lse.getFirstIndex());
                    //            System.out.println(", "+lse.getLastIndex());
                    jTable1.changeSelection(selectedRow, selectedColumn, false, false);
                    if(selectedColumn==0 || selectedColumn==2) jTable1.changeSelection(selectedRow, selectedColumn+1, true, true);
                    else jTable1.changeSelection(selectedRow, selectedColumn-1, true, true);
                    tempC = selectedColumn;
                    selectionIsChanging = false;
                }
            }
        });
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jTable1.setDefaultRenderer(java.lang.String.class, cellRenderer);

        java.util.Set forward = new java.util.HashSet(jTable1.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forward.add(javax.swing.KeyStroke.getKeyStroke("TAB"));
        jTable1.setFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
        java.util.Set backward = new java.util.HashSet(jTable1.getFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backward.add(javax.swing.KeyStroke.getKeyStroke("shift TAB"));
        jTable1.setFocusTraversalKeys(java.awt.KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel6.setBackground(getPanelBackground());
        jPanel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 0, 0));
        jPanel6.setLayout(new java.awt.GridLayout(2, 2, 2, 5));

        jLabel12.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel12.setForeground(getPanelForeground());
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("jLabel12");
        jLabel12.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel12.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showTable(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jPanel6.add(jLabel12);

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel11.setForeground(getPanelForeground());
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("jLabel11");
        jLabel11.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel11.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showTable(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jPanel6.add(jLabel11);

        jLabel13.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel13.setForeground(getPanelForeground());
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("jLabel13");
        jLabel13.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel13.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showTable(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jPanel6.add(jLabel13);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel10.setForeground(getPanelForeground());
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("jLabel10");
        jLabel10.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                showTable(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jPanel6.add(jLabel10);

        jPanel1.add(jPanel6, java.awt.BorderLayout.PAGE_START);

        jPanel7.setBackground(getPanelBackground());
        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        jCheckBox1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCheckBox1.setForeground(getPanelForeground());
        jCheckBox1.setSelected(symbolOn);
        jCheckBox1.setText("Show Symbols");
        jCheckBox1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jCheckBox1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jPanel7.add(jCheckBox1);
        jPanel7.add(filler1);

        jCheckBox3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCheckBox3.setForeground(getPanelForeground());
        jCheckBox3.setSelected(ampmTime);
        jCheckBox3.setText("Show 12 hour clock format");
        jCheckBox3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jCheckBox3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });
        jPanel7.add(jCheckBox3);
        jPanel7.add(filler2);

        jCheckBox2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jCheckBox2.setForeground(getPanelForeground());
        jCheckBox2.setSelected(blackbg);
        jCheckBox2.setText("Contrast view");
        jCheckBox2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jCheckBox2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                mouseOverLabel(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                mouseOverExited(evt);
            }
        });
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        jCheckBox2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jCheckBox2FocusGained(evt);
            }
        });
        jPanel7.add(jCheckBox2);

        jPanel1.add(jPanel7, java.awt.BorderLayout.PAGE_END);

        jTabbedPane1.addTab("Main", jPanel1);

        java.awt.GridBagLayout jPanel2Layout = new java.awt.GridBagLayout();
        jPanel2Layout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel2Layout.rowHeights = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        jPanel2.setLayout(jPanel2Layout);

        jLabel1.setText("Select Country:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        jPanel2.add(jLabel1, gridBagConstraints);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(getCountryNames()));
        jComboBox1.setActionCommand("comboBox1Changed");
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jComboBox1, gridBagConstraints);

        jLabel2.setText("Select City:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel2, gridBagConstraints);

        jComboBox2.setEditable(true);
        jComboBox2.setModel(getCityNames(jComboBox1.getModel().getSelectedItem().toString()));
        jComboBox2.setActionCommand("comboBox2Changed");
        jComboBox2.setName("cityBox"); // NOI18N
        final javax.swing.JTextField edit = (javax.swing.JTextField)jComboBox2.getEditor().getEditorComponent();
        edit.addKeyListener(new java.awt.event.KeyAdapter() {
            String tmpStr="", perma="";
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char keyChar = evt.getKeyChar();
                if(keyChar==evt.VK_TAB || keyChar==evt.VK_ENTER || keyChar==evt.VK_ESCAPE) {
                    edit.setText(perma);
                    if(jComboBox2.isPopupVisible()) jComboBox2.hidePopup();
                } else {
                    if(!jComboBox2.isPopupVisible()) jComboBox2.showPopup();
                }
                tmpStr = edit.getText();
                for(int x=0; x<cityNames.getSize(); x++) {
                    if(cityNames.getElementAt(x).toString().toLowerCase().startsWith(tmpStr.toLowerCase())) {
                        jComboBox2.setSelectedIndex(x);
                        perma = cityNames.getElementAt(x).toString();
                        edit.setText(tmpStr);
                        return;
                    }
                }
            }
        });
        edit.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
        });
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jComboBox2, gridBagConstraints);

        jLabel3.setText("Latitude:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel5.setText("Longitude:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel5, gridBagConstraints);

        jTextField3.setName("latDeg"); // NOI18N
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fillZeroOnFocusLost(evt);
            }
        });
        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                lengthVerifier(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jTextField3, gridBagConstraints);

        jLabel6.setText(":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jLabel6, gridBagConstraints);

        jTextField4.setName("latMin"); // NOI18N
        jTextField4.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fillZeroOnFocusLost(evt);
            }
        });
        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                lengthVerifier(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jTextField4, gridBagConstraints);

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "N", "S" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jComboBox3, gridBagConstraints);

        jTextField1.setName("longDeg"); // NOI18N
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fillZeroOnFocusLost(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                lengthVerifier(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jTextField1, gridBagConstraints);

        jLabel4.setText(":");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jLabel4, gridBagConstraints);

        jTextField2.setName("longMin"); // NOI18N
        jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                fillZeroOnFocusLost(evt);
            }
        });
        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                lengthVerifier(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jTextField2, gridBagConstraints);

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "E", "W" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jComboBox4, gridBagConstraints);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Time Zone GMT Diff:");

        jTextField5.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField5.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                selectTextOnFocus(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField5)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(jPanel3, gridBagConstraints);

        jButton1.setText("Add Place");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton1);

        jButton2.setText("Remove Place");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton2);

        jButton3.setText("Reset Place");
        jButton3.setActionCommand("Reset");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 17;
        jPanel2.add(jPanel4, gridBagConstraints);

        jSeparator2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.ipadx = 350;
        jPanel2.add(jSeparator2, gridBagConstraints);

        dateChooser1.setDate(selectedDate);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_TRAILING;
        jPanel2.add(dateChooser1, gridBagConstraints);

        jButton4.setText("Reset Date");
        jButton4.setActionCommand("Reset");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        jPanel2.add(jButton4, gridBagConstraints);

        jLabel9.setText("Set Date:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 24;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel2.add(jLabel9, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 28;
        gridBagConstraints.gridwidth = 19;
        gridBagConstraints.ipadx = 350;
        jPanel2.add(jSeparator1, gridBagConstraints);

        jButton5.setText("Apply");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton5);

        jButton6.setText("Cancel");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel5.add(jButton6);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 19;
        jPanel2.add(jPanel5, gridBagConstraints);

        jTabbedPane1.addTab("Settings", jPanel2);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabel15.setBackground(new java.awt.Color(0, 0, 0));
        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 30)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("<html>Planetary<p>Hour</html>");
        jLabel15.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jLabel15.setOpaque(true);
        jPanel8.add(jLabel15, java.awt.BorderLayout.PAGE_START);
        jPanel8.add(filler3, java.awt.BorderLayout.WEST);

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel9.add(filler4);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel8.setText("<html>PlanetHour v1.0 - Planetary hour calculation software<br>Copyright (C) 2014 Sounak Choudhury</html>");
        jPanel9.add(jLabel8);
        jPanel9.add(filler5);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel16.setText(getMultilineHTMLString(aboutString, jLabel16.getFontMetrics(jLabel16.getFont()), 330));
        jLabel16.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel9.add(jLabel16);
        jPanel9.add(filler6);

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("<html>Contact E-mail: <a href='mailto:sounak_s@rediffmail.com'>sounak_s@rediffmail.com</a></html>");
        jPanel9.add(jLabel14);

        jPanel8.add(jPanel9, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("About", jPanel8);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
        if (formUpdateSource.equalsIgnoreCase("none")) {
            formUpdateSource = evt.getActionCommand();
        }
        if (formUpdateSource.equalsIgnoreCase(evt.getActionCommand())) {
            getCityNames(jComboBox1.getModel().getSelectedItem().toString());
            updateSettingsForm(jComboBox1.getSelectedItem().toString(), jComboBox2.getSelectedItem().toString());

        }
    }//GEN-LAST:event_jComboBox1ActionPerformed
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        int sel = JOptionPane.showConfirmDialog(this, "Really Exit? Press Yes to exit or No to minimize", formTitle, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        switch(sel) {
            case JOptionPane.YES_OPTION:
                shutdown();
                break;
            case JOptionPane.NO_OPTION:
                setState(ICONIFIED);
                break;
            default:
                //do nothing
                break;
        }
        //shutdown();
    }//GEN-LAST:event_formWindowClosing
    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        if (formUpdateSource.equalsIgnoreCase("none")) {
            formUpdateSource = evt.getActionCommand();
        }
        if (formUpdateSource.equalsIgnoreCase(evt.getActionCommand())) {
            jComboBox1.setSelectedItem(getSelectedCountry());
            getCityNames(jComboBox1.getModel().getSelectedItem().toString());
            jComboBox2.setSelectedItem(getSelectedCity());
            updateSettingsForm(getSelectedCountry(), getSelectedCity());
        }
    }//GEN-LAST:event_jButton3ActionPerformed
    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
        if (formUpdateSource.equalsIgnoreCase("none")) {
            formUpdateSource = evt.getActionCommand();
        }
        if (formUpdateSource.equalsIgnoreCase(evt.getActionCommand())) {
            updateSettingsForm(jComboBox1.getSelectedItem().toString(), jComboBox2.getSelectedItem().toString());
        }
    }//GEN-LAST:event_jComboBox2ActionPerformed
    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        // TODO add your handling code here:
        if(running) showHideTable();
        if (formUpdateSource.equalsIgnoreCase("none") && jTabbedPane1.getSelectedIndex() == 1) {
            formUpdateSource = "tabPane1";
        } else if(jTabbedPane1.getSelectedIndex() == 2) {
            boolean isMorning = curTime <= allPlanets[14].getEndTime();
            java.awt.Image image = isMorning?Planet.getEarthSunImage(getClass()):Planet.getEarthMoonImage(getClass());
            jLabel15.setIcon(new javax.swing.ImageIcon(image.getScaledInstance(300, 200, java.awt.Image.SCALE_AREA_AVERAGING)));
        }
        if (formUpdateSource.equalsIgnoreCase("tabPane1")) {
            jComboBox1.setSelectedItem(getSelectedCountry());
            getCityNames(jComboBox1.getModel().getSelectedItem().toString());
            jComboBox2.setSelectedItem(getSelectedCity());
            updateSettingsForm(getSelectedCountry(), getSelectedCity());
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        String alertMsg = verifyInputs();
        if (alertMsg != null) {
            JOptionPane.showMessageDialog(this, alertMsg, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String lat = jTextField3.getText() + jTextField4.getText();
            String lon = jTextField1.getText() + jTextField2.getText();
            PlaceRecord rec = new PlaceRecord(jComboBox2.getSelectedItem().toString(), lat,
                    jComboBox3.getSelectedItem().toString().equalsIgnoreCase("N"), lon,
                    jComboBox4.getSelectedItem().toString().equalsIgnoreCase("E"), jTextField5.getText());
            alertMsg = addCity(jComboBox1.getSelectedItem().toString(), jComboBox2.getSelectedItem().toString(), lat, lon,
                    jComboBox3.getSelectedItem().toString(), jComboBox4.getSelectedItem().toString(), rec.getTimezone().getID().substring(3));
            if (alertMsg.equals("")) { // do nothing
            } else if (alertMsg.startsWith("Error")) {
                JOptionPane.showMessageDialog(this, alertMsg, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, alertMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            if (formUpdateSource.equalsIgnoreCase("none")) {
                formUpdateSource = evt.getActionCommand();
            }
            if (formUpdateSource.equalsIgnoreCase(evt.getActionCommand())) {
                String b4upd = jComboBox2.getModel().getSelectedItem().toString();
                getCityNames(jComboBox1.getModel().getSelectedItem().toString());
                jComboBox2.setSelectedItem(b4upd);
                updateSettingsForm(jComboBox1.getModel().getSelectedItem().toString(), b4upd);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    private void lengthVerifier(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lengthVerifier
        // TODO add your handling code here:
        String name = evt.getComponent().getName();
        if(evt.getComponent() instanceof javax.swing.JTextField) {
            javax.swing.JTextField srcField = (javax.swing.JTextField) evt.getSource();
            if (srcField.getSelectedText() != null) {
                int i = srcField.getSelectionStart();
                srcField.setText(srcField.getText().replace(srcField.getSelectedText(), ""));
                srcField.setCaretPosition(i);
            }
            int len = ((javax.swing.JTextField) evt.getSource()).getText().length();
            try {
                if ((name.equalsIgnoreCase("latdeg") || name.equalsIgnoreCase("longdeg")) && len >= 3) {
                    srcField.setText(srcField.getText(0, 3));
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    evt.consume();
                } else if ((name.equalsIgnoreCase("latmin") || name.equalsIgnoreCase("longmin")) && len >= 2) {
                    srcField.setText(srcField.getText(0, 2));
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    evt.consume();
                }
            } catch (javax.swing.text.BadLocationException ex) {
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_lengthVerifier
    private void fillZeroOnFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fillZeroOnFocusLost
        // TODO add your handling code here:
        String name = evt.getComponent().getName();
        javax.swing.JTextField src = (javax.swing.JTextField) evt.getSource();
        int i = src.getText().length();
        switch (i) {
            case 0:
                src.setText("00");
                break;
            case 1:
                src.setText("0" + src.getText());
                break;
        }
        if ((name.equalsIgnoreCase("latdeg") || name.equalsIgnoreCase("longdeg")) && i < 3) {
            src.setText("0" + src.getText());
        }
    }//GEN-LAST:event_fillZeroOnFocusLost
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        String alertMsg = verifyInputs();
        if (alertMsg != null) {
            JOptionPane.showMessageDialog(this, alertMsg, "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            alertMsg = removeCity(jComboBox1.getSelectedItem().toString(), jComboBox2.getSelectedItem().toString());
            if (alertMsg.equals("")) { // do nothing
            } else if (alertMsg.startsWith("Error")) {
                JOptionPane.showMessageDialog(this, alertMsg, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, alertMsg, "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            selectedCity = getSelectedPlaceRecord();
            if (formUpdateSource.equalsIgnoreCase("none")) {
                formUpdateSource = evt.getActionCommand();
            }
            if (formUpdateSource.equalsIgnoreCase(evt.getActionCommand())) {
                jComboBox1.setSelectedItem(getSelectedCountry());
                getCityNames(jComboBox1.getModel().getSelectedItem().toString());
                jComboBox2.setSelectedItem(getSelectedCity());
                updateSettingsForm(getSelectedCountry(), getSelectedCity());
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    private void selectTextOnFocus(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_selectTextOnFocus
        // TODO add your handling code here:
        java.awt.Component comp = evt.getComponent();
        if (comp instanceof javax.swing.JTextField) {
            ((javax.swing.JTextField) comp).selectAll();
        }
    }//GEN-LAST:event_selectTextOnFocus
    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        dateChooser1.setDate(new Date());
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        boolean applyChanges=false, databaseError=false, dateError=false;
        String country=null, city=null;
        String alertMsg = verifyInputs();
        if (alertMsg == null) {
            String lat = jTextField3.getText() + jTextField4.getText();
            String lon = jTextField1.getText() + jTextField2.getText();
            PlaceRecord rec = new PlaceRecord(jComboBox2.getSelectedItem().toString(), lat,
                    jComboBox3.getSelectedItem().toString().equalsIgnoreCase("N"), lon,
                    jComboBox4.getSelectedItem().toString().equalsIgnoreCase("E"), jTextField5.getText());
            try {
                PreparedStatement ps1 = con.prepareStatement("select * from APP.City where Country like '" + jComboBox1.getSelectedItem().toString()
                        + "' and City like '" + jComboBox2.getSelectedItem().toString() + "'");
                ResultSet rs = ps1.executeQuery();
                if(rs.next()) {
                    if(rs.getString("Lat").equals(jTextField3.getText() + jTextField4.getText())
                            && rs.getString("Lon").equals(jTextField1.getText() + jTextField2.getText())
                            && rs.getString("NoSo").equals(jComboBox3.getSelectedItem().toString())
                            && rs.getString("EaWe").equals(jComboBox4.getSelectedItem().toString())
                            && rs.getString("GMTDiff").equals(rec.getTimezone().getID().substring(3))) {
                            try {
                                selectedDate = dateChooser1.getDate();
                                applyChanges = true;
                                threadStop();
                                
                                country = jComboBox1.getSelectedItem().toString();
                                city = jComboBox2.getSelectedItem().toString();
                            } catch(NullPointerException ex) {
                                dateError = true;
                            }
                    }
                }
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                databaseError = true;
                Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if(databaseError) {
            JOptionPane.showMessageDialog(this, "An error occurred while fetching database. Please restart the application.", "Error", JOptionPane.ERROR_MESSAGE);            
        } else if(dateError) {
            JOptionPane.showMessageDialog(this, "The date entered is invalid. Please enter a valid date.", "Error", JOptionPane.ERROR_MESSAGE);
        } else if(applyChanges) {
            boolean succ = setSelectedPlaceRecord(country, city);
            if(!succ) JOptionPane.showMessageDialog(this, "Either a database error has occurred or the selected city is not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
            else {
                cal.setTime(selectedDate);
                curTime = selectedDate.getTime();
                fillPlanetsArray(selectedCity, selectedDate);
                updateMainForm(selectedCity);
                showHideSymbols(selectedCells);
                resizeColumns();
                jTabbedPane1.setSelectedIndex(0);
                threadStart();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Unsaved changes were found in country / city / latitude / longitude / time-zone. Please click Add Place to save the location information first.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        showHideSymbols(selectedCells);
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void mouseOverLabel(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseOverLabel
        // TODO add your handling code here:
        java.awt.Component labelComponent = evt.getComponent();
        labelComponent.setForeground(Color.blue);
    }//GEN-LAST:event_mouseOverLabel

    private void mouseOverExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mouseOverExited
        // TODO add your handling code here:
        java.awt.Component labelComponent = evt.getComponent();
        labelComponent.setForeground(getPanelForeground());
    }//GEN-LAST:event_mouseOverExited

    private void showTable(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showTable
        // TODO add your handling code here:
        showHideTable();
    }//GEN-LAST:event_showTable

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
        blackbg = !blackbg;
        changeBackground(true);
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        Point p = evt.getPoint();
        selectedRow = jTable1.rowAtPoint(p);
        selectedColumn = jTable1.columnAtPoint(p);
        int arrayPosChaldean=-1;
        int arrayPosHinduSp=-1;
        if(selectedColumn==1) selectedColumn=0;
        else if(selectedColumn==3) selectedColumn=2;
        for(int i=0; i<Planet.chaldean.length; i++) {
            if(jTable1.getValueAt(selectedRow, selectedColumn).toString().startsWith(Planet.chaldean[i])) {
                arrayPosChaldean = i;
                break;
            }
        }
        for (int i = 0; i < Planet.hinduSp.length; i++) {
            if (jTable1.getValueAt(selectedRow, selectedColumn).toString().startsWith(Planet.hinduSp[i])) {
                arrayPosHinduSp = i;
                break;
            }
        }
        evt.consume();
        if(arrayPosChaldean!=-1) {
            String msg = getMultilineHTMLString(Planet.getProperty(Planet.chaldean[arrayPosChaldean]), getFontMetrics(getFont()), 450);
            ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+Planet.chaldean[arrayPosChaldean].toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
            icon1.setImage(icon1.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_FAST));
            JOptionPane.showMessageDialog(this, msg, Planet.chaldean[arrayPosChaldean], JOptionPane.INFORMATION_MESSAGE, icon1);
        } else if (arrayPosHinduSp!=-1) {
            String msg = getMultilineHTMLString(Planet.getProperty(Planet.hinduSp[arrayPosHinduSp]), getFontMetrics(getFont()), 450);
            ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+Planet.hinduSp[arrayPosHinduSp].toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
            icon1.setImage(icon1.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_FAST));
            JOptionPane.showMessageDialog(this, msg, Planet.hinduSp[arrayPosHinduSp].equalsIgnoreCase("yamag")?"Yamagandam":Planet.hinduSp[arrayPosHinduSp], JOptionPane.INFORMATION_MESSAGE, icon1);
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        // TODO add your handling code here:
        ampmTime = !ampmTime;
        updateMainForm(selectedCity);
        showHideSymbols(selectedCells);
        resizeColumns();
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jTable1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()!= java.awt.event.KeyEvent.VK_ENTER) {
            evt.consume();
            return;
        }
        selectedRow = jTable1.getSelectedRow();
        selectedColumn = jTable1.getSelectedColumn();
        int arrayPosChaldean=-1;
        int arrayPosHinduSp=-1;
        if(selectedColumn==1) selectedColumn=0;
        else if(selectedColumn==3) selectedColumn=2; 
        for(int i=0; i<Planet.chaldean.length; i++) {
            if(jTable1.getValueAt(selectedRow, selectedColumn).toString().startsWith(Planet.chaldean[i])) {
                arrayPosChaldean = i;
                break;
            }
        }
        for (int i = 0; i < Planet.hinduSp.length; i++) {
            if (jTable1.getValueAt(selectedRow, selectedColumn).toString().startsWith(Planet.hinduSp[i])) {
                arrayPosHinduSp = i;
                break;
            }
        }
        evt.consume();
        if(arrayPosChaldean!=-1) {
            String msg = getMultilineHTMLString(Planet.getProperty(Planet.chaldean[arrayPosChaldean]), getFontMetrics(getFont()), 450);
            ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+Planet.chaldean[arrayPosChaldean].toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
            icon1.setImage(icon1.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_FAST));
            JOptionPane.showMessageDialog(this, msg, Planet.chaldean[arrayPosChaldean], JOptionPane.INFORMATION_MESSAGE, icon1);
        } else if (arrayPosHinduSp!=-1) {
            String msg = getMultilineHTMLString(Planet.getProperty(Planet.hinduSp[arrayPosHinduSp]), getFontMetrics(getFont()), 450);
            ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+Planet.hinduSp[arrayPosHinduSp].toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
            icon1.setImage(icon1.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_FAST));
            JOptionPane.showMessageDialog(this, msg, Planet.hinduSp[arrayPosHinduSp].equalsIgnoreCase("yamag")?"Yamagandam":Planet.hinduSp[arrayPosHinduSp], JOptionPane.INFORMATION_MESSAGE, icon1);
        }
    }//GEN-LAST:event_jTable1KeyTyped

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        // TODO add your handling code here:
        if(tray==null) return;
        if(evt.getNewState()==ICONIFIED) {
            try {
                trayIcon.setImage(getTrayImage());
                trayIcon.setToolTip("Hour of "+thl.getName()+" till "+Planet.getFormattedTime(thl.getEndTime(), selectedCity.timeZone, true, false));
                tray.add(trayIcon);
                setVisible(false);
            } catch (java.awt.AWTException ex) {
                System.out.println("unable to add to tray");
            }
        }
        if(evt.getNewState()==7) {
            try{
                trayIcon.setImage(getTrayImage());
                trayIcon.setToolTip("Hour of "+thl.getName()+" till "+Planet.getFormattedTime(thl.getEndTime(), selectedCity.timeZone, true, false));
                tray.add(trayIcon);
                setVisible(false);
            }catch(java.awt.AWTException ex){
                System.out.println("unable to add to system tray");
            }
        }
        if(evt.getNewState()==MAXIMIZED_BOTH) {
            tray.remove(trayIcon);
            setVisible(true);
        }
        if(evt.getNewState()==NORMAL) {
            tray.remove(trayIcon);
            setVisible(true);
        }
    }//GEN-LAST:event_formWindowStateChanged
    private void restoreWindow() {
        setVisible(true);
        setExtendedState(javax.swing.JFrame.NORMAL);
        //maximize
    }
    private void menuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItem1ActionPerformed
        // TODO add your handling code here:
        restoreWindow();
    }//GEN-LAST:event_menuItem1ActionPerformed

    private void menuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItem2ActionPerformed
        // TODO add your handling code here:
        shutdown();
    }//GEN-LAST:event_menuItem2ActionPerformed

    private void jTable1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseMoved
        // TODO add your handling code here:
        Point p = evt.getPoint();
        int row = jTable1.rowAtPoint(p);
        int col = jTable1.columnAtPoint(p);
        if ((row > -1 && row < jTable1.getRowCount()) && (col > -1 && col < jTable1.getColumnCount())) {
            if (currCell == null || (currCell.x != col || currCell.y != row)) {
                currCell = new Point(col, row);
                cellRenderer.setMouseOverCell(currCell);
                jTable1.repaint();
            }
        }
    }//GEN-LAST:event_jTable1MouseMoved

    private void jTable1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusGained
        // TODO add your handling code here:
        if(!showTable) showHideTable();
        selectionIsChanging = true;
        if(jTable1.hasFocus() && selectedColumn==-1) {
            jTable1.changeSelection(selectedRow, 0, true, false);
            jTable1.changeSelection(selectedRow, 1, true, true);
        }
        else if(selectedColumn==0 || selectedColumn==2) {
            jTable1.changeSelection(selectedRow, selectedColumn, true, false);
            jTable1.changeSelection(selectedRow, selectedColumn+1, true, true);
        }
        else {
            jTable1.changeSelection(selectedRow, selectedColumn, true, false);
            jTable1.changeSelection(selectedRow, selectedColumn-1, true, true);
        }
        selectionIsChanging = false;
    }//GEN-LAST:event_jTable1FocusGained

    private void jTable1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable1FocusLost
        // TODO add your handling code here:
        selectedColumn = jTable1.getSelectedColumn();
        selectedRow = jTable1.getSelectedRow();
        if (selectedColumn > -1 && selectedRow > -1) {
            selectionIsChanging = true;
            jTable1.changeSelection(selectedRow, selectedColumn, true, false);
            selectionIsChanging = false;
        }
    }//GEN-LAST:event_jTable1FocusLost

    private void jCheckBox2FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jCheckBox2FocusGained
        // TODO add your handling code here:
        if(!showTable) showHideTable();
    }//GEN-LAST:event_jCheckBox2FocusGained

    private void jTable1MouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseExited
        // TODO add your handling code here:
        currCell=new Point(-1, -1);
        cellRenderer.setMouseOverCell(currCell);
        jTable1.repaint();
    }//GEN-LAST:event_jTable1MouseExited
    private String fillZeroInText(String text, int fillLength) {
        int i = text.length();
        StringBuilder fin = new StringBuilder(text);
        if (i < fillLength) {
            for (int x = 0; x < fillLength - i; x++) {
                fin.insert(0, '0');
            }
        }
        return fin.toString();
    }
    
    private void resizeColumns() {
        javax.swing.table.TableColumn tbc = null;
        int prefWidth = SwingUtilities.computeStringWidth(jTable1.getFontMetrics(jTable1.getFont()), ampmTime?"88:88a-88:88p":"88:88-88:88")-60;
        for (int i=3; i>=0; i--) {
            tbc = jTable1.getColumnModel().getColumn(i);
            if(i==0 || i==2) {
                tbc.setPreferredWidth(-1);
            } else {
                tbc.setPreferredWidth(prefWidth);
            }
        }
    }
    
    /**
     * Toggles visibility of planetary hour table and only current hours on each
     * subsequent call
     */
    public void showHideTable() {
        showTable = !showTable;
        if(!showTable) {
            currBounds = getBounds();
            if(jScrollPane1.isShowing()) setBounds(getX(), getY(), getWidth(), jScrollPane1.getLocationOnScreen().y-getY()+6);
        } else {
            currBounds.setLocation(getX(), getY());
            setBounds(currBounds);
        }
    }
    
    /**
     * This is the primary method which calculates sunrise and sunset times and fills
     * the planetary hour array for the planetary date given and the given place
     * @param currec The PlaceRecord for which planetary hours are to be calculated
     * @param date selected date for calculating planetary hour
     */
    private void fillPlanetsArray(PlaceRecord currec, Date date) {
        // Get calendar instance based on Place record time zone. Set the date parameter to this calendar.
        Calendar cal2 = Calendar.getInstance(currec.getTimezone());
        cal2.setTime(date);
        long nowTime = cal2.getTime().getTime();
        if (debug) System.out.println("Now time: " + nowTime + " " + Planet.formattedTimeString(nowTime, cal2.getTimeZone(), ampmTime, false));
        long time12am = currec.get12amTime(date);
        if (debug) System.out.println("12 AM time: " + Planet.formattedTimeString(time12am, cal2.getTimeZone(), ampmTime, false));
        long todayRise = currec.getRiseOrSetTime(date, true);
        if (debug) System.out.println("Rise time: " + todayRise + " " + Planet.formattedTimeString(todayRise, cal2.getTimeZone(), ampmTime, false));
        // Set calendar to previous date if the given date(time) is after 12 am but before sunrise...

        if (nowTime >= time12am && nowTime < todayRise) {
            if (debug) System.out.println("Nowtime is after midnight");
            if (cal2.get(Calendar.DATE) == 1) {
                if (cal2.get(Calendar.MONTH) == Calendar.JANUARY) {
                    cal2.set(Calendar.YEAR, cal2.get(Calendar.YEAR) - 1);
                    cal2.set(Calendar.MONTH, Calendar.DECEMBER);
                    cal2.set(Calendar.DATE, 31);
                } else {
                    cal2.set(Calendar.MONTH, cal2.get(Calendar.MONTH) - 1);
                    cal2.set(Calendar.DATE, PlaceRecord.getLastDayOfMonth(cal2.get(Calendar.MONTH), cal2.get(Calendar.YEAR)));
                }
            } else {
                cal2.set(Calendar.DATE, cal2.get(Calendar.DATE) - 1);
            }
        }
        // =========================== This part fills planet day values in planets ===========
        // Get the planet sequence no. according to weekday and init the Planet elements
        int planetSeqNo = 0;
        int daySeqNo = 0;
        switch (cal2.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                planetSeqNo = 3;
                daySeqNo = 0;
                break;
            case Calendar.MONDAY:
                planetSeqNo = 6;
                daySeqNo = 1;
                break;
            case Calendar.TUESDAY:
                planetSeqNo = 2;
                daySeqNo = 2;
                break;
            case Calendar.WEDNESDAY:
                planetSeqNo = 5;
                daySeqNo = 3;
                break;
            case Calendar.THURSDAY:
                planetSeqNo = 1;
                daySeqNo = 4;
                break;
            case Calendar.FRIDAY:
                planetSeqNo = 4;
                daySeqNo = 5;
                break;
            case Calendar.SATURDAY:
                planetSeqNo = 0;
                daySeqNo = 6;
                break;
        }
        for (int j = 0; j < 24; j++) {
            planets[j] = new Planet(Planet.chaldean[planetSeqNo], 0, 0, false);
            if (planetSeqNo == 6) {
                planetSeqNo = 0;
            } else {
                planetSeqNo++;
            }
        }
        //=====================================================================================
        // Now get the new, so formed, date...
        Date date2 = cal2.getTime();
        // Get sunrise and sunset time of the date
        todayRise = currec.getRiseOrSetTime(date2, true);
        long todaySet = currec.getRiseOrSetTime(date2, false);
        // Set the sunrise in the calendar using date
        date2.setTime(todayRise);
        cal2.setTime(date2);
        // Get number of millisec per hour.
        long dayDuration = todaySet - todayRise;
        long milliSecPerHr = (long) Math.floor(dayDuration / 12);
        //if(debug) System.out.println("Today rise: "+Planet.formattedTimeString(todayRise, cal3.getTimeZone())+", "+"Today set: "+Planet.formattedTimeString(todaySet, cal3.getTimeZone()));
        if (debug) System.out.println("Millisec per hour: " + milliSecPerHr);
        long tmpTime = cal2.getTime().getTime();
        for (int y = 0; y < 12; y++) {
            planets[y].setStartTime(tmpTime);
            planets[y].setDay(true);
            //if(debug) System.out.println("Calendar="+Planet.getFormattedTime(tmpTime, cal3.getTimeZone()));
            tmpTime += milliSecPerHr;
            planets[y].setEndTime(tmpTime);
            // simultaneously fill all planets array
            allPlanets[y] = planets[y];
        }

        // Set date to today sunset and the calendar to the date.
        date2.setTime(todaySet);
        cal2.setTime(date2);
        // Get tomorrow day of the year.
        long tomrwRise = currec.getNextDayRiseOrSetTime(date2, true);
        // Get number of millisec per hour
        long nightDuration = tomrwRise - todaySet;
        milliSecPerHr = (long) Math.floor(nightDuration / 12);
        if (debug) System.out.println("Today set: " + Planet.formattedTimeString(todaySet, cal2.getTimeZone(), ampmTime, false) + ", " + "Tomorrow rise: " + Planet.formattedTimeString(tomrwRise, cal2.getTimeZone(), ampmTime, false));
        if (debug) System.out.println("Millisec per hour: " + milliSecPerHr);
        tmpTime = cal2.getTime().getTime();
        for (int y = 12; y < 24; y++) {
            planets[y].setStartTime(tmpTime);
            planets[y].setDay(false);
            //if(debug) System.out.println("Calendar="+Planet.getFormattedTime(tmpTime, cal3.getTimeZone()));
            tmpTime += milliSecPerHr;
            planets[y].setEndTime(tmpTime);
            // simultaneously fill all planets array
            allPlanets[y] = planets[y];
        }
        // Now work with spPlanets
        final double rahuSeq[] = new double[]{0.875, 0.125, 0.75, 0.5, 0.625, 0.375, 0.25};
        final double gulikaSeq[] = new double[]{0.75, 0.625, 0.5, 0.375, 0.25, 0.125, 0};
        final double yamaGSeq[] = new double[]{0.5, 0.375, 0.25, 0.125, 0, 0.75, 0.625};
        for (int z = 0; z < 6; z++) {
            spPlanet[z] = new Planet(Planet.hinduSp[z % 3], 0, 0, z < 3);
            if (z < 3) {
                milliSecPerHr = (long) Math.floor(dayDuration * 0.125);
            } else {
                milliSecPerHr = (long) Math.floor(nightDuration * 0.125);
            }
            if (z % 3 == 0) {
                if (z < 3) {
                    tmpTime = todayRise + (long) Math.floor(dayDuration * rahuSeq[daySeqNo]);
                } else {
                    tmpTime = todaySet + (long) Math.floor(nightDuration * (1-rahuSeq[daySeqNo]) - milliSecPerHr);
                }
            } else if (z % 3 == 1) {
                if (z < 3) {
                    tmpTime = todayRise + (long) Math.floor(dayDuration * gulikaSeq[daySeqNo]);
                } else {
                    tmpTime = todaySet + (long) Math.floor(nightDuration * (1-gulikaSeq[daySeqNo]) - milliSecPerHr);
                }
            } else if (z % 3 == 2) {
                if (z < 3) {
                    tmpTime = todayRise + (long) Math.floor(dayDuration * yamaGSeq[daySeqNo]);
                } else {
                    tmpTime = todaySet + (long) Math.floor(nightDuration * (1-yamaGSeq[daySeqNo]) - milliSecPerHr);
                }
            }
            spPlanet[z].setStartTime(tmpTime);
            spPlanet[z].setEndTime(tmpTime + milliSecPerHr);
            allPlanets[24 + z] = spPlanet[z];
        }
        // sort the spPlanet array in ascending order of start time
        int noOfComp = spPlanet.length - 1;
        Planet temp;
        for (int a = 0; a < spPlanet.length - 1; ++a) {
            for (int b = 0; b < noOfComp; ++b) {
                if (spPlanet[b].getStartTime() > spPlanet[b + 1].getStartTime())//changed operator to print in ascending order
                {
                    temp = spPlanet[b];
                    spPlanet[b] = spPlanet[b + 1];
                    spPlanet[b + 1] = temp;
                }

            }
            --noOfComp;
        }
        // sort the allPlanets array into ascending order of startTime
        noOfComp = allPlanets.length - 1;
        for (int a = 0; a < allPlanets.length - 1; ++a) {
            for (int b = 0; b < noOfComp; ++b) {
                if (allPlanets[b].getStartTime() > allPlanets[b + 1].getStartTime())//changed operator to print in ascending order
                {
                    temp = allPlanets[b];
                    allPlanets[b] = allPlanets[b + 1];
                    allPlanets[b + 1] = temp;
                }
            }
            --noOfComp;
        }
        //calculations for next day after rise
        daySeqNo = daySeqNo + 1 == 7 ? 0 : daySeqNo + 1;
        nextDay1stPlanet = new Planet(Planet.chaldean[planetSeqNo], planets[planets.length - 1].getEndTime(), planets[planets.length - 1].getEndTime() + milliSecPerHr, true);
        long rTime = tomrwRise + (long) Math.floor(dayDuration * rahuSeq[daySeqNo]);
        long gTime = tomrwRise + (long) Math.floor(dayDuration * gulikaSeq[daySeqNo]);
        long yTime = tomrwRise + (long) Math.floor(dayDuration * yamaGSeq[daySeqNo]);
        long fTime = Math.min(rTime, Math.min(gTime, yTime));
        milliSecPerHr = (long) Math.floor(dayDuration * 0.125);

        if (fTime == rTime) {
            nextDay1stSpPlanet = new Planet(Planet.hinduSp[0], rTime, rTime + milliSecPerHr, true);
        } else if (fTime == gTime) {
            nextDay1stSpPlanet = new Planet(Planet.hinduSp[1], gTime, gTime + milliSecPerHr, true);
        } else if (fTime == yTime) {
            nextDay1stSpPlanet = new Planet(Planet.hinduSp[2], yTime, yTime + milliSecPerHr, true);
        }
        cal2 = null;
    }
    
    private void checkIfAlreadyRunning() {
        ProgramLock lock = new ProgramLock("PlanetHour");
        if (lock.isAppActive()) {
            System.out.println("Already active.");
            JOptionPane.showMessageDialog(this, "PlanetHour is already running!", "PlanetHour - Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);    
        }
    }

    /**
     *
     * @param args the command line arguments
     *
     */
    public static void main(String args[]) {        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MyFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MyFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MyFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MyFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                MyFrame frame = new MyFrame();
                frame.checkIfAlreadyRunning();
                frame.setVisible(true);
                frame.showHideTable();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private hour.DateChooser dateChooser1;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private java.awt.MenuItem menuItem1;
    private java.awt.MenuItem menuItem2;
    private java.awt.PopupMenu popupMenu1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void run() {
        Date updater = new Date();
        curTime = cal.getTime().getTime();
        boolean trigger=false;
        String name1, time1;
        while (oneSecThread != null && running) {
            long diff = System.currentTimeMillis() - sysTime;
            sysTime = System.currentTimeMillis();
            curTime += diff;
            cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 1);
            if (debug) System.out.println("Planet time: " + nhl.getStartTime() + ", Current Time: " + curTime);
            
            name1 = spl.getName();
            time1 = Planet.getNextPlanetTimeString(spl, curTime, selectedCity.getTimezone(), false, ampmTime, ampmTime);
            if(name1.equalsIgnoreCase("rahu")) name1 = "Rahu kaalam";
            else if(name1.equalsIgnoreCase("gulika")) name1 = "Gulika kaalam";
            else if(name1.equalsIgnoreCase("yamag")) name1 = "Yamagandakam";

            if(cal.get(Calendar.SECOND)%5==0) trigger = !trigger;
            if(trigger) {
                setTitle(formTitle+" - "+selectedCity.place_name+", "+selectedCountry);
                if (tray != null && trayIcon !=null) {
                    trayIcon.setImage(getTrayImage());
                    trayIcon.setToolTip("Hour of "+thl.getName()+" till "+Planet.getFormattedTime(thl.getEndTime(), selectedCity.timeZone, true, false));
                }
            }
            else {
                setTitle(Planet.getDayName(cal.get(Calendar.DAY_OF_WEEK), false)+", "+Planet.formattedTimeString(curTime, selectedCity.getTimezone(), ampmTime, false));
                if (tray != null && trayIcon != null) {
                    trayIcon.setImage(time1.startsWith("in")?getTrayImage():spl.getImage(symbolOn, getClass()));
                    trayIcon.setToolTip(time1.startsWith("in")?"Hour of "+thl.getName()+" till "+Planet.getFormattedTime(thl.getEndTime(), selectedCity.timeZone, true, false):name1+" till "+Planet.getFormattedTime(spl.getEndTime(), selectedCity.timeZone, true, false));
                }
            }

            if(cal.get(Calendar.SECOND) % 60 == 0) {
                updater.setTime(curTime);
                cal.setTime(updater);
                jLabel10.setText(nhl.getName()+" hour "+Planet.getNextPlanetTimeString(nhl, curTime, selectedCity.getTimezone(), false, ampmTime, false));
                jLabel10.setIcon(new ImageIcon(getClass().getResource("/hour/"+nhl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))));
                jLabel13.setText(name1+(time1.startsWith("in")?" ":": ")+time1);
                jLabel13.setIcon(new ImageIcon(getClass().getResource("/hour/"+spl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))));
            }
            
            if (running == false) break;
            if (curTime > thl.getEndTime() || curTime > spl.getEndTime())// checks if next planet start time exceeds current time
            {
                if (debug) System.out.println("Update triggered!");
                if (curTime > planets[planets.length - 1].getEndTime()) {
                    fillPlanetsArray(selectedCity, selectedDate); // triggers fillPlanetsArray in case of day change
                }
                updateMainForm(selectedCity);
                showHideSymbols(selectedCells);
                resizeColumns();
            }

            if (running == false) break;
            try {
                oneSecThread.sleep(1000);
            } catch (InterruptedException ex) {
                if (oneSecThread != null) {
                    oneSecThread.interrupt();
                }
            }
        }
        updater = null;
        if (debug) System.out.println("Run finished");
    }

    /**
     * Changes the contents of mainForm component.
     * @param rec PlaceRecord, based on which the method updates the main form
     */
    private void updateMainForm(PlaceRecord rec) {
        javax.swing.table.TableModel modelToReturn = jTable1.getModel();
        selectedCells.clear();
        jTable1.removeAll();
        if (debug) System.out.println("Main list is not null");
        if (debug) System.out.println("Place name is " + rec.place_name);
        java.util.TimeZone tz = rec.getTimezone();
        jLabel12.setText("Day of " + planets[0].getName()+" till "+Planet.getFormattedTime(planets[planets.length-1].getEndTime(), tz, ampmTime, false));
        jLabel12.setIcon(new ImageIcon(getClass().getResource("/hour/"+planets[0].getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))));
        thl = getThisHourLord();
        nhl = getNextHourLord(thl);
        spl = getSpecialPlanetNearby(false);
        Planet spl2 = getSpecialPlanetNearby(true); // this for selected planets in jTable1
        ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+thl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))); 
        icon1.setImage(icon1.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_FAST));
        jLabel11.setText(thl.getName()+" hour running");
        jLabel11.setIcon(icon1);
        jLabel10.setText(nhl.getName()+" hour "+Planet.getNextPlanetTimeString(nhl, curTime, tz, false, ampmTime, false));
        jLabel10.setIcon(new ImageIcon(getClass().getResource("/hour/"+nhl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))));
        String name1 = spl.getName();
        String time1 = Planet.getNextPlanetTimeString(spl, curTime, selectedCity.getTimezone(), false, ampmTime, ampmTime);
        if(name1.equalsIgnoreCase("rahu")) name1 = "Rahu kaalam";
        else if(name1.equalsIgnoreCase("gulika")) name1 = "Gulika kaalam";
        else if(name1.equalsIgnoreCase("yamag")) name1 = "Yamagandakam";
        jLabel13.setText(name1+(time1.startsWith("in")?" ":": ")+time1);
        jLabel13.setIcon(new ImageIcon(getClass().getResource("/hour/"+spl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2.png":".png"))));
        if (debug) System.out.println("updateMainList(), thisLord: " + thl.getName());
        Point point;
        String hiphen = ampmTime?"-":" - ";
        StringBuilder buffer1 = new StringBuilder(30);
        for (int i = 0; i < allPlanets.length; i++) {
            for (int j = 0; j < 4; j++) {
                modelToReturn.setValueAt(allPlanets[i].getName(), i < 15 ? i : i - 15, i < 15 ? 0 : 2);
                buffer1.append(Planet.getFormattedTime(allPlanets[i].getStartTime(), tz, ampmTime, true));
                buffer1.append(hiphen);
                buffer1.append(Planet.getFormattedTime(allPlanets[i].getEndTime(), tz, ampmTime, true));
                modelToReturn.setValueAt(buffer1.toString(), i < 15 ? i : i - 15, i < 15 ? 1 : 3);
                buffer1.setLength(0);
                if(thl.equals(allPlanets[i]) || spl2 != null && spl2.equals(allPlanets[i])) {
                    point = new Point((i < 15 ? i : i - 15), (i < 15 ? 0 : 2));
                    if(!selectedCells.contains(point)) selectedCells.add(point);
                    point = new Point((i < 15 ? i : i - 15), (i < 15 ? 1 : 3));
                    if(!selectedCells.contains(point)) selectedCells.add(point);
                }
            }
        }
        if (debug) System.out.println("updateMainList(): Completed!");
    }

    /**
     * Gets the planet which owns this planetary hour
     * @return Planet which owns this hour
     */
    public synchronized Planet getThisHourLord() {
        long tmp1, tmp2;
//        long nowTime = System.currentTimeMillis();
        long nowTime = curTime;
        if (debug) {
            System.out.println("getThisHourLord: nowTime=" + nowTime);
        }
        for (int i = 0; i < planets.length; i++) {
            tmp1 = planets[i].getStartTime();
            tmp2 = planets[i].getEndTime();
            if (tmp2 > nowTime && nowTime >= tmp1) {
                if (debug) {
                    System.out.println("getThisHourLord(): " + planets[i].getName() + ". Match found!");
                }
                return planets[i];
            }
        }
        return null;
    }

    /**
     * Gets the Planet which owns the next planetary hour based on the current planetary
     * hour given.
     * @param curPlanet the Planet for the current planetary hour
     * @return Planet for the next planetary hour
     */
    public synchronized Planet getNextHourLord(Planet curPlanet) {
        if (curPlanet == null) {
            return null;
        } else if (curPlanet.getName().equals("NA")) {
            return null;
        }
        long tmp1, tmp2;
        long nowTime = curPlanet.getStartTime();
        for (int i = 0; i < planets.length; i++) {
            tmp1 = planets[i].getStartTime();
            tmp2 = planets[i].getEndTime();
            if (debug) {
                System.out.println("getNextHourLord(): " + tmp1 + ", " + nowTime + ", " + tmp2);
            }
            if (tmp2 > nowTime && nowTime >= tmp1) {
                if (i == 23) {
                    return nextDay1stPlanet;
                }
                if (debug) {
                    System.out.println("getNextHourLord(): " + planets[i + 1].getName() + ". Match found!");
                }
                return planets[i + 1];
            }
        }
        return nextDay1stPlanet;
    }

    /**
     * If forThisHourOnly is true it gets the special planet for this hour, if any.
     * Else it searches and returns the next special planet after this hour
     * @param forThisHourOnly if true, it searches special planet for this hour only
     * @return Special planet for this hour or next nearest, whichever required
     */
    public Planet getSpecialPlanetNearby(boolean forThisHourOnly) {
        long tmp1, tmp2;
//        long nowTime = System.currentTimeMillis();
        long nowTime = curTime;
        for (int i = 0; i < spPlanet.length; i++) {
            tmp1 = spPlanet[i].getStartTime();
            tmp2 = spPlanet[i].getEndTime();
            if (tmp2 > nowTime && nowTime >= tmp1) {
                if (debug) {
                    System.out.println("getSpecialPlanetNearby(): " + spPlanet[i].getName() + ". Match found!");
                }
                return spPlanet[i];
            }
        }
        if (debug) {
            System.out.println("getSpecialPlanetNearby: cannot find in 1st loop");
        }
        if (!forThisHourOnly) {
            if (debug) {
                System.out.println("getSpecialPlanetNearby: Not this hour only");
            }
            for (int i = 0; i < spPlanet.length; i++) {
                tmp1 = spPlanet[i].getStartTime();
                if (tmp1 > nowTime) {
                    if (debug) {
                        System.out.println("getSpecialPlanetNearby(): " + spPlanet[i].getName() + ". Match found!");
                    }
                    return spPlanet[i];
                }
            }
        }
        if (debug) {
            System.out.println("getSpecialPlanetNearby: cannot find in 2nd loop");
        }
        return nextDay1stSpPlanet;
    }

    /**
     * Toggles background of main form on each subsequent call, from general to contrast
     * (black) mode and vise-versa
     * @param applyToComponents if false, it will only change the variables without repainting
     */
    private void changeBackground(boolean applyToComponents) {
        if(!blackbg) {
            tableBackground = tableBGColor;
            tableForeground = tableFGColor;
            panelBackground = panelBGColor;
            panelForeground = panelFGColor;
        } else {
            tableBackground = tableContrastBGColor;
            tableForeground = tableContrastFGColor;
            panelBackground = panelContrastBGColor;
            panelForeground = panelContrastFGColor;
        }
        if(applyToComponents) {
            jPanel6.setBackground(getPanelBackground());
            jPanel7.setBackground(getPanelBackground());
            jTable1.setBackground(getTableBackground());
//            jLabel8.setForeground(getPanelForeground());
            jCheckBox3.setForeground(getPanelForeground());
            jLabel10.setForeground(getPanelForeground());
            jLabel11.setForeground(getPanelForeground());
            jLabel12.setForeground(getPanelForeground());
            jLabel13.setForeground(getPanelForeground());
            jCheckBox1.setForeground(getPanelForeground());
            jCheckBox2.setForeground(getPanelForeground());
            jTable1.setForeground(getTableForeground());
        }
    }

    /**
     * Toggles all images from planets to planet symbols and vise-versa on each subsequent call
     * @param selectedCellsList list of cells in the table which needs to appear selected always. Its an ArrayList of Point object in which X resembles row and Y, a column
     */
    private void showHideSymbols(ArrayList<Point> selectedCellsList) {
        symbolOn = jCheckBox1.isSelected();
//        jTable1.setDefaultRenderer(java.lang.String.class, new PlanetCellRenderer(symbolOn, selectedCellsList));
        cellRenderer.setSelectedCells(selectedCells);
        cellRenderer.setShowSymbol(symbolOn);
        jTable1.setDefaultRenderer(java.lang.String.class, cellRenderer);
        jTable1.repaint();
        ImageIcon icon1 = new ImageIcon(getClass().getResource("/hour/"+planets[0].getName().toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
        icon1.setImage(icon1.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_FAST));
        jLabel12.setIcon(icon1);
        jLabel12.repaint();
        icon1 = new ImageIcon(getClass().getResource("/hour/"+thl.getName().toLowerCase()+(jCheckBox1.isSelected()?"2big.png":"big.png"))); 
        icon1.setImage(icon1.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_FAST));
        jLabel11.setText(thl.getName()+" hour running");
        jLabel11.setIcon(icon1);
        jLabel11.repaint();
        jLabel10.setIcon(new ImageIcon(getClass().getResource("/hour/"+nhl.getName().toLowerCase()+(symbolOn?"2.png":".png"))));
        jLabel10.repaint();
        jLabel13.setIcon(new ImageIcon(getClass().getResource("/hour/"+spl.getName().toLowerCase()+(symbolOn?"2.png":".png"))));
        jLabel13.repaint();
    }
    
    /**
     * Create a multi-line i.e. "p" tag embedded html string which fits in the given width in pixels
     * @param singleString the string to be made multi-line
     * @param metrics font metrics from the JComponent where the multi-line string needs to be set
     * @param maxWidth width of the multi-line strings allowed
     * @return html tagged multi-line string (through "p" tag)
     */
    public static String getMultilineHTMLString(String singleString, java.awt.FontMetrics metrics, int maxWidth) {
        String tokenString[] = singleString.split("\\s");
        StringBuilder builded = new StringBuilder("<html>");
        StringBuilder builder = new StringBuilder();
        for (String string : tokenString) {
            builder.append(string);
            builder.append(' ');
            if(SwingUtilities.computeStringWidth(metrics, builder.toString()) > maxWidth || string.equals("")) {
                builded.append(builder);
                builded.append("<br>");
                builder.setLength(0);
            }
        }
        if(builder.length()>0) builded.append(builder);
        builder.setLength(0);
        builded.append("</html>");
        return builded.toString();
    }
    
    public javax.swing.Icon getTrayImageIcon() {
        if(isVisible() && tray != null) {
            return jLabel11.getIcon();
        } else {
            return new ImageIcon(Planet.getEarthImage(getClass()));
        }
    }

    public java.awt.Image getTrayImage() {
        if(tray != null) {
            return thl.getImage(symbolOn, getClass());
        } else {
            return Planet.getEarthImage(getClass());
        }
    }

    private void initTray() {
        if(java.awt.SystemTray.isSupported()) {
            tray=java.awt.SystemTray.getSystemTray();
            trayIcon=new java.awt.TrayIcon(Planet.getEarthImage(getClass()), "PlanetHour", popupMenu1);
            trayIcon.setImageAutoSize(true);
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent me) {
                    if(me.getButton()==java.awt.event.MouseEvent.BUTTON1 && me.getClickCount()==2) restoreWindow();
                }
            });
        }else{
            System.out.println("system tray not supported");
        }
    }

    private void shutdown() {
        threadStop();
        if (oneSecThread != null) {
            running = false;
            if (oneSecThread.isAlive()) {
                try {
                    oneSecThread.join();
                } catch (InterruptedException ex) {
                    oneSecThread.interrupt();
                } finally {
                    oneSecThread = null;
                }
            }
            oneSecThread = null;
        }
        try {
            // TODO add your handling code here:
            ps = con.prepareStatement("update APP.City set Lat = '"+(getState()==NORMAL?getX():X)+"', Lon = '"+(getState()==NORMAL?getY():Y)+"', NoSo = '"+(blackbg?"S":"N")+"', EaWe = '"+(symbolOn?"W":"E")+"', GMTDiff = '"+(ampmTime?"T":"F")+"' where City like 'Desktop' and Country like 'Desktop'");
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(MyFrame.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.exit(0);
        }
    }
}
