/*
 * File: PlaceRecord.java in java package com.sounaks.planethour is part of application
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


import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Sounak Choudhury
 */
public class PlaceRecord
{
    /**
     * Place Name for this place record
     */
    public String place_name;
    /**
     * Latitude of the place in this place record
     */
    public String latitude;
    /**
     * Hemisphere of the latitude of the place in this place record
     */
    public boolean north_south;
    /**
     * Longitude of the place in this place record
     */
    public String longitude;
    /**
     * Hemisphere of the longitude of the place in this place record
     */
    public boolean east_west;
    /**
     * Time zone of the place as String
     */
    public String time_zone;
    /**
     * Time zone of the place as TimeZone
     */
    public TimeZone timeZone;
    private static boolean debug = false;
    volatile double suncal;
    private static final Calendar usageCal=Calendar.getInstance();

    /**
     * Creates new PlaceRecord with default blank place name and zero values for latitude,
     * longitude, north and east as hemispheres and time zone as GMT+0
     */
    public PlaceRecord()
    {
        this.place_name="";
        this.latitude="00000";
        this.north_south=true;
        this.longitude="00000";
        this.east_west=true;
        this.time_zone="0.0";
        timeZone = getZone();
//        usageCal.setTimeZone(timeZone);
//        usageCal = Calendar.getInstance(getZone());
    }

    /**
     * Creates a PlaceRecord with the supplied ';' delimited String as place details
     * @param record A ';' delimited String with six parts, namely:<p>
     * 1) Place name<p>
     * 2) Latitude<p>
     * 3) Latitude hemisphere (north as true and south as false)<p>
     * 4) Longitude<p>
     * 5) Longitude hemisphere (east as true and west as false)<p>
     * 6) Time zone as GMT+ time difference
     */
    public PlaceRecord(String record)
    {
        String recordString[]=record.split(";"); //split(record,';');
        this.place_name=recordString[0];
        this.latitude=recordString[1];
        this.north_south=recordString[2].equalsIgnoreCase("true");
        this.longitude=recordString[3];
        this.east_west=recordString[4].equalsIgnoreCase("true");
        this.time_zone=recordString[5];
        timeZone = getZone();
//        usageCal.setTimeZone(timeZone);
//        usageCal = Calendar.getInstance(getZone());
    }

    /**
     * Creates a PlaceRecord with the given parameters
     * @param name Place name String
     * @param latitude Latitude as String
     * @param ns Latitude hemisphere as boolean (north as true and south as false)
     * @param longitude Longitude as String
     * @param ew Longitude hemisphere as boolean (east as true and west as false)
     * @param timezone Time zone as GMT+ time difference as String
     */
    public PlaceRecord(String name, String latitude, boolean ns, String longitude, boolean ew, String timezone)
    {
        this.place_name=name;
        this.latitude=latitude;
        this.north_south=ns;
        this.longitude=longitude;
        this.east_west=ew;
        this.time_zone=timezone;
        timeZone = getZone();
//        usageCal.setTimeZone(timeZone);
//        usageCal = Calendar.getInstance(getZone());
    }

    /**
     * Just a place record.
     * @return A PlaceRecord storing place details of New Delhi
     */
    public static PlaceRecord capital()
    {
        PlaceRecord newRecord=new PlaceRecord("New Delhi, IN;02836;true;07712;true;5.5");
        return newRecord;
    }

    /**
     * Gets the latitude of this place in decimal representation
     * @return A double containing the latitude of this place in decimal format
     */
    public double getDecimalLatitude()
    {
        int signum = north_south ? 1 : -1;
        String tmp[] = split(latitude);
        if(debug) System.out.println("Lat:"+tmp[0]+'.'+tmp[1]+'.');
        int deg = Integer.parseInt(tmp[0]);
        double min = (Integer.parseInt(tmp[1]))/60D;
        return ((double)deg+min) * (double)signum;
    }

    /**
     * Gets the longitude of this place in decimal representation
     * @return A double containing the longitude of this place in decimal format
     */
    public double getDecimalLongitude()
    {
        int signum = east_west ? 1 : -1;
        String tmp[] = split(longitude);
        if(debug) System.out.println("Long:"+tmp[0]+'.'+tmp[1]+'.');
        int deg = Integer.parseInt(tmp[0]);
        double min = (Integer.parseInt(tmp[1]))/60D;
        return ((double)deg+min) * (double)signum;
    }

    /**
     * Gets the time zone of this place in decimal representation
     * @return A double containing the time zone of this place in decimal format
     */
    public double getDecimalTimezone()
    {
        char sepChar = time_zone.indexOf(':') == -1 ? '.' : ':';
        char charAtZero = time_zone.charAt(0);
        int signum = charAtZero == '-' ? -1 : 1;
        String tmp[] = time_zone.split(sepChar==':'?":":"\\.");
        if(charAtZero == '+' || charAtZero == '-')
            tmp[0] = tmp[0].substring(1);
        int hr = Integer.parseInt(tmp[0]);
        //System.out.println(Trigonometry.round(Integer.parseInt(tmp[1])/60D,2));
        double min = sepChar==':' ? Trigonometry.round((Integer.parseInt(tmp[1]))/60D, 2) : Double.parseDouble("0."+tmp[1]);
        return (hr+min) * signum;
    }

    /**
     * Gets the time zone of this place as a java TimeZone object
     * @return A TimeZone object containing the time zone of this place
     */
    private TimeZone getZone()
    {
        StringBuilder finalStr=new StringBuilder(10);
        finalStr.append("GMT");
        char sepChar = time_zone.indexOf(':') == -1 ? '.' : ':';
        char charAtZero = time_zone.charAt(0);
        char signum = charAtZero == '-' ? '-' : '+';
        finalStr.append(signum);
        String tmp[] = time_zone.split(sepChar==':'?":":"\\.");
        if(charAtZero == '+' || charAtZero == '-')
            tmp[0] = tmp[0].substring(1);
        if(tmp[0].length()==1) finalStr.append('0');
        finalStr.append(tmp[0]);
        finalStr.append(':');
        int min = sepChar==':' ? Integer.parseInt(tmp[1]) : (int)Math.floor(Double.parseDouble("0."+tmp[1])*60D);
        if(min<9) finalStr.append('0');
        finalStr.append(min);
        return TimeZone.getTimeZone(finalStr.toString());
    }

    /**
     * Gets the time zone of this place record
     * @return The time zone as TimeZone
     */
    public final TimeZone getTimezone()
    {
        return timeZone;
    }
    
    @Override
    public String toString()
    {
        return place_name+';'+latitude+';'+north_south+';'+longitude+';'+east_west+';'+time_zone;
    }

    /**
     * Splits a degree into degree and minute and returns them as a String array
     * @param decimalDegree The degree in decimal format to be split
     * @return A String array containing the degree and minute of the decimal degree given as parameter
     */
    public static String[] toDegreeMinute(double decimalDegree)
    {
        //preserving the signum to get the needed results from ceil/floor.
        int signum = Trigonometry.getSign(decimalDegree);
        //getting absolute value to get needed results from ceil/floor.
        decimalDegree = Math.abs(decimalDegree);
        int deg = (int)Math.floor(decimalDegree);
        if(deg <= -180D || deg >= 180D)
        {
            deg = 0;
            throw new IllegalArgumentException("Degree: >= 180 OR <= -180");
        }
        double tmpmin = 60D * (Math.abs(decimalDegree)-deg);
        int min = (int)Math.floor(tmpmin);
        if((tmpmin - min) >= 0.5D) min += 1;
        deg *= signum; //adding sign after all calculations.
        return new String[]{String.valueOf(deg), String.valueOf(min)};
    }

    /**
     * As the name suggests, this method splits the given <code>String</code> strToSplit into parts and returns an array of containing them. The splitting position(s) are determined by the <code>String</code> delimiter.
     * Note: The strings in the resulting array does not contain the delimiter.
     * @param strToSplit This is the <code>String</code> to split.
     * @return A <code>String[]</code> array containing the parts of the <code>String</code> strToSplit.
     */
    public static String[] split(String strToSplit)
    {
        if(strToSplit==null || strToSplit.equals(""))
            throw new NullPointerException("String to split is NULL.");
        int len = strToSplit.length();
        if(len < 2) throw new NullPointerException("Either degrees or minutes is NULL.");
        String toReturn[]=new String[2];
        toReturn[0] = strToSplit.substring(0, len-2);
        toReturn[1] = strToSplit.substring(len-2);
        return toReturn;
    }

    /**
     * Gets the invalid characters in the place name as a <code>String</String>.
     * @param name The name / place name <code>String</code> to be parsed for extracting invalid characters.
     * @param charsToFind A <code>String</code> of valid characters.
     * @return Returns a <code>String</code> representing the invalid characters.
     */
    public static String getInvalidChars(String name, String charsToFind)
    {
        char testChars[] = charsToFind.toCharArray();
        StringBuilder falseChars = new StringBuilder("");
        boolean found;
        for(int i=0; i<name.length(); i++)
        {
            found=false;
            char tmp = name.charAt(i);
            for(int j=0; j<testChars.length; j++)
            {
                if(tmp==testChars[j])
                {
                    found=true;
                    continue;
                }
            }
            if(!found)
                falseChars.append(tmp);
        }
        if(falseChars.length()==0) return null;
        else return falseChars.toString();
    }

    /**
     * Checks error in the latitude entry and returns an error <code>String</code> if any found
     * @param deg Degree of the latitude
     * @param min minute of the latitude
     * @return An error <code>String</code> and null if no error
     */
    public static String checkLatitudeError(String deg, String min)
    {
        String latitudeInvalidChars = getInvalidChars(deg+min,"1234567890");
        String errorString="Latitude: ";
        if(deg==null || min==null || deg.equals("") || min.equals("") || latitudeInvalidChars!=null) return errorString+="blank or invalid characters: "+latitudeInvalidChars;
        else
        {
            int d=Integer.parseInt(deg);
            int m=Integer.parseInt(min);
            if(d<0 || d>89) return errorString+="degree should be between 0 and 89";
            else if(m<0 || m>59) return errorString+="minute should be between 0 and 59";
            else return null;
        }
    }

    /**
     * Checks error in the longitude entry and returns an error <code>String</code> if any found
     * @param deg Degree of the longitude
     * @param min Minute of the longitude
     * @return  An error <code>String</code> and null if no error
     */
    public static String checkLongitudeError(String deg, String min)
    {
        String longitudeInvalidChars = getInvalidChars(deg+min,"1234567890");
        String errorString="Longitude: ";
        if(deg==null || min==null || deg.equals("") || min.equals("") || longitudeInvalidChars!=null) return errorString+="blank or invalid characters: "+longitudeInvalidChars;
        else
        {
            int d=Integer.parseInt(deg);
            int m=Integer.parseInt(min);
            if(d<0 || d>179) return errorString+="degree should be between 0 and 179";
            else if(m<0 || m>59) return errorString+="minute should be between 0 and 59";
            else return null;
        }
    }

    /**
     * Checks error in the time zone entry and returns an error <code>String</code> if any found
     * @param text Time zone in hour:min or hour.decimal minute
     * @return An error <code>String</code> and null if no error
     */
    public static String checkTimezoneError(String text)
    {
        String timeZoneInvalidChars = getInvalidChars(text,"-+:.1234567890");
        String errorString="Timezone: ";
        if(text==null || text.equals("") || timeZoneInvalidChars!=null) return errorString+="blank or invalid characters: "+timeZoneInvalidChars; //tests for blank, null and invalid characters.
        else
        {
            int dot=text.indexOf('.');
            int colon=text.indexOf(':');
            if(dot==-1 && colon==-1) return errorString+="missing time separator '.' or ':'"; //tests if none of the separators exist
            else if(dot!=-1 && colon!=-1) return errorString+="both the time separators '.' and ':' should not co-exist"; //tests if both the separators exist
            else //either of the separators exist
            {
                if((dot+1)==text.length() || (colon+1)==text.length()) return errorString+="'.' or ':' should not be last the character"; //tests if the existing separator is the last char in the: text
                else if(dot==0 || colon==0) return errorString+="'.' or ':' should not be the first character"; //tests if the existing separator is the first char in the: text
                else if(text.indexOf('.',dot+1)!=-1 || text.indexOf(':', colon+1)!=-1) return errorString+="double usage of '.' or ':' character is not allowed"; //tests if the existing separator repeats itself
                else // separator occurs in the mid and does not repeat itself
                {
                    int minus=text.indexOf('-');
                    int plus=text.indexOf('+');
                    if(minus!=-1 && plus!=-1) return errorString+="both '-' and '+' sign should not co-exist"; //tests if both the signs exist
                    else //either or none of the signs exists
                    {
                        if(minus>0 || plus>0) return errorString+="signs '-' or '+' should be at the beginning only"; //tests if the existing sign occurs in the mid of text or is not the first char
                        else if((minus+1)==text.length() || (plus+1)==text.length()) return errorString+="signs '+' or '-' should be at the beginning only"; //tests if the existing sign is the last char
                        else if(text.indexOf('-', minus+1)!=-1 || text.indexOf('+', plus+1)!=-1) return errorString+="repeatation of sign is not allowed"; //tests if existing sign repeats itself
                        else //either or none of the sign does not occur in mid or end of text i.e. occurs at first pos and does not repeats itself
                        {
                            if(text.startsWith("+:") || text.startsWith("+.") || text.startsWith("-:") || text.startsWith("-.")) return errorString+="':' or '.' should not be immedietly after '-' or '+'";
                            else
                            {
                                if(dot>0)
                                {
                                    String zonePart[]=text.split(String.valueOf("\\.")); //split(text,'.');
                                    int hr=Math.abs(Integer.parseInt(zonePart[0].startsWith("+")?zonePart[0].substring(1):zonePart[0]));
                                    //int min=(int)Math.floor(Double.parseDouble("0."+zonePart[1])*60);
                                    if(hr>23 || hr<0) return errorString+="hour should be between 0 and 23";
                                    else return null;
                                }
                                else
                                {
                                    String zonePart[]=text.split(String.valueOf(':')); //split(text,':');
                                    int hr=Math.abs(Integer.parseInt(zonePart[0].startsWith("+")?zonePart[0].substring(1):zonePart[0]));
                                    int min=Integer.parseInt(zonePart[1]);
                                    if(hr>23 || hr<0) return errorString+="hour should be between 0 and 23";
                                    else if(min>59 || min<0) return errorString+="minute should be between 0 and 59";
                                    else return null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets the number of days since beginning of this year and thisDay, getting the nth day of year.
     * @param date The Date for which day of year to be calculated
     * @return The day of year counting from the beginning of the year.
     */
    public int getDayOfYear(Date date)
    {
        usageCal.setTimeZone(timeZone);
        usageCal.setTime(date);
        int month = usageCal.get(Calendar.MONTH)+1;
        int day = usageCal.get(Calendar.DAY_OF_MONTH);
        int year = usageCal.get(Calendar.YEAR);
        return (int)(Math.floor(275D*month/9D)-Math.floor((month+9D)/12D)*(1D+Math.floor((year-4D*Math.floor(year/4D)+2D)/3D)))+day-30;
    }

    /**
     * Gets the number of days since beginning of this year and thisDay + 1, getting the nth day of year.
     * @param date The Date for which the next day of year is to be calculated
     * @return The day of year counting from the beginning of the year till nextDay.
     */
    public int getNextDayOfYear(Date date)
    {
        usageCal.setTimeZone(timeZone);
        usageCal.setTime(date);
        if(isLastDayOfMonth(usageCal.get(Calendar.DAY_OF_MONTH), usageCal.get(Calendar.MONTH), usageCal.get(Calendar.YEAR)))
        {
            if(usageCal.get(Calendar.MONTH) == Calendar.DECEMBER)
            {
                usageCal.set(Calendar.YEAR, usageCal.get(Calendar.YEAR)+1);
                usageCal.set(Calendar.MONTH, Calendar.JANUARY);
                usageCal.set(Calendar.DATE, 1);
            }
            else
            {
                usageCal.set(Calendar.MONTH, usageCal.get(Calendar.MONTH)+1);
                usageCal.set(Calendar.DATE, 1);
            }
        }
        else
        {
            usageCal.set(Calendar.DATE, usageCal.get(Calendar.DATE)+1);
        }
        int month = usageCal.get(Calendar.MONTH)+1;
        int day = usageCal.get(Calendar.DAY_OF_MONTH);
        int year = usageCal.get(Calendar.YEAR);
        return (int)(Math.floor(275D*month/9D)-Math.floor((month+9D)/12D)*(1+Math.floor((year-4D*Math.floor(year/4D)+2D)/3D)))+day-30;
    }

    /**
     * Verifies the given date is the last date of the given month
     * @param dayOfMonth Day of month
     * @param month The month from Calendar.JANUARY to Calendar.DECEMBER
     * @param year The year
     * @return True if the date is last day of the given month, False otherwise
     */
    public static boolean isLastDayOfMonth(int dayOfMonth, int month, int year)
    {
        switch (month)
        {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                if(dayOfMonth == 31) return true;
                else return false;
                //break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                if(dayOfMonth == 30) return true;
                else return false;
                //break;
            case Calendar.FEBRUARY:
                if ((year % 4 == 0) && !(year % 100 == 0) || (year % 400 == 0))
                {
                    if(dayOfMonth == 29) return true;
                    else return false;
                }
                else
                {
                    if(dayOfMonth == 28) return true;
                    else return false;
                }
                //break;
            default:
                return false;
                //break;
        }
    }

    /**
     * Gets the last day of the given month in the given year
     * @param month The given month from Calendar.JANUARY to Calendar.DECEMBER
     * @param year The given year
     * @return The last day of the month from 0 to 31
     */
    public static int getLastDayOfMonth(int month, int year)
    {
        switch (month)
        {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                return 31;
                //break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                return 30;
                //break;
            case Calendar.FEBRUARY:
                if ((year % 4 == 0) && !(year % 100 == 0) || (year % 400 == 0))
                {
                    return 29;
                }
                else
                {
                    return 28;
                }
                //break;
            default:
                return 0;
                //break;
        }
    }

    /**
     * Convert the longitude to hour value and calculate an approximate time in year
     * @param dayOfYear The nth day of year counting from the beginning of year where, n=1 for 1-January
     * @param longi The longitude for which hour value is to be calculated
     * @param sunrise If the desired hour is sunrise then true
     * @return An approximate time in year adding number of days and hours
     */
    private double sunsTrueLongitude(double timeOfYear)
    {
        // Calculate the Sun's mean anomaly
        suncal = 0.9856D * timeOfYear - 3.289D;
        // Calculate the Sun's true longitude
        suncal = suncal + 1.916D * Trigonometry.sinD(suncal) + 0.020D * Trigonometry.sinD(2D * suncal) + 282.634D;

  	if(suncal <= -360D) suncal += 360D;
	if(suncal >= 360D) suncal -= 360D;
        return suncal;
    }

    private double sunsRightAscension(double sunTrueLong)
    {
	suncal = Trigonometry.atanD(0.91764D * Trigonometry.tanD(sunTrueLong));
	if(debug) System.out.println("RA before = "+suncal);
	if(suncal <= -360D)
	  suncal += 360D;
	if(suncal >= 360D)
	  suncal -= 360D;
	if(debug) System.out.println("RA after = "+suncal);
	//NOTE: suncal potentially needs to be adjusted into the range [0,360) by adding/subtracting 360

	suncal = suncal + (Math.floor(sunTrueLong/90D)) * 90D - (Math.floor(suncal/90D)) * 90D;
	suncal = suncal / 15D;
        return suncal;
    }

    private double sunsLocalHourAngle(double sunTrueLong, double sunZenith, double lati, boolean sunrise)
    {
	suncal = 0.39782D * Trigonometry.sinD(sunTrueLong);

	suncal = (Trigonometry.cosD(sunZenith) - suncal * Trigonometry.sinD(lati)) / (Trigonometry.cosD(Trigonometry.asinD(suncal)) * Trigonometry.cosD(lati));
	if(debug) System.out.println("cosH = "+suncal);
	if (suncal >  1D && debug)
	  System.out.println("the sun never rises on this location (on the specified date)");
	if (suncal < -1D && debug)
	  System.out.println("the sun never sets on this location (on the specified date)");

	if(sunrise) // if rising time is desired:
	  suncal = 360D - Trigonometry.acosD(suncal);
	else
	  suncal = Trigonometry.acosD(suncal);
	if(debug) System.out.println("H = "+suncal);
	return suncal / 15D;
    }

    private synchronized double getRiseOrSetTime(int dayOfYear, boolean rise)
    {
        double lngHour, timeOfYear, stl, slh, UT;
        // Convert the longitude to hour value and calculate an approximate time of year
        lngHour = getDecimalLongitude()/15D;
        timeOfYear = rise ? (double)dayOfYear+(6D-lngHour)/24D : (double)dayOfYear+(18D-lngHour)/24D;

        stl = sunsTrueLongitude(timeOfYear);
        slh = sunsLocalHourAngle(stl, 90.8333D, getDecimalLatitude(), rise);

        UT = slh + sunsRightAscension(stl) - 0.06571D * timeOfYear - 6.622D - lngHour;
        //NOTE: UT potentially needs to be adjusted into the range [0,24) by adding/subtracting 24
        if(UT >= 24D) UT -= 24D;
        else if(UT < 0D) UT += 24D;
        // Convert UT value to local time zone of latitude/longitude
        UT = Math.abs(UT + getDecimalTimezone());
        if(UT >= 24D) UT -= 24D;
        else if(UT < 0D) UT += 24D;
        if(debug) System.out.println("UT = "+UT+", Timezone = "+getDecimalTimezone());
        return Math.abs(UT);
    }

    /**
     * Gets the time at 12 AM, as long value, for the given date
     * @param date The given date as Date
     * @return long date value
     */
    public synchronized long get12amTime(Date date)
    {
        usageCal.setTimeZone(timeZone);
        usageCal.setTime(date);
        // Zero down hour, minute, and second fields
        usageCal.set(Calendar.HOUR_OF_DAY, 0);
        usageCal.set(Calendar.MINUTE, 0);
        usageCal.set(Calendar.SECOND, 0);
        
        return usageCal.getTime().getTime();
    }

    /**
     * Gets sunrise or sunset time in long for the given Date
     * @param date The given Date
     * @param rise true for sunrise and false for sunset
     * @return Date in long value
     */
    public synchronized long getRiseOrSetTime(Date date, boolean rise)
    {
//        usageCal.setTime(date);
        int thisDay = getDayOfYear(date);
//        if(debug) System.out.println("PlaceRecord.getRiseOrSetTime(): "+Planet.formattedTimeString(get12amTime(date), usageCal.getTimeZone()));
        if(debug) System.out.println("PlaceRecord.getRiseOrSetTime().thisDay: "+thisDay);
        return get12amTime(date) + Trigonometry.getMilliSecs(getRiseOrSetTime(thisDay, rise));
    }

    /**
     * Gets sunrise or sunset time in long for next day from the given Date
     * @param date The given Date
     * @param rise true for sunrise and false for sunset
     * @return Date in long value
     */
    public synchronized long getNextDayRiseOrSetTime(Date date, boolean rise)
    {
        usageCal.setTimeZone(timeZone);
        usageCal.setTime(date);
        // Adjust cale to nextDay 12 AM
        if(PlaceRecord.isLastDayOfMonth(usageCal.get(Calendar.DAY_OF_MONTH), usageCal.get(Calendar.MONTH), usageCal.get(Calendar.YEAR)))
        {
            if(usageCal.get(Calendar.MONTH) == Calendar.DECEMBER)
            {
                usageCal.set(Calendar.YEAR, usageCal.get(Calendar.YEAR)+1);
                usageCal.set(Calendar.MONTH, Calendar.JANUARY);
                usageCal.set(Calendar.DATE, 1);
            }
            else
            {
                usageCal.set(Calendar.MONTH, usageCal.get(Calendar.MONTH)+1);
                usageCal.set(Calendar.DATE, 1);
            }
        }
        else
        {
            usageCal.set(Calendar.DATE, usageCal.get(Calendar.DATE)+1);
        }
        // Get nextDay day of the year.
        int nextDay = getNextDayOfYear(date);
        return get12amTime(usageCal.getTime()) + Trigonometry.getMilliSecs(getRiseOrSetTime(nextDay, rise));
    }
}
