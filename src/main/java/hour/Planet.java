/*
 * File: Planet.java in java package hour is part of application
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

/**
 *
 * @author Sounak Choudhury
 */
import java.awt.Color;
import java.awt.Graphics;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;

/**
 *
 * @author Sounak Choudhury
 */
public class Planet
{
    private String name;
    private long startTime;
    private long endTime;
    private boolean day;
    private Image selfImage;
    private static Image sun, star, search, moon, NA, earthsun, earthmoon, earth;
    private static boolean debug=false;
    public static final String[] hinduSp = new String[]{"Rahu", "Gulika", "YamaG"};
    public static final String[] chaldean = new String[]{"Saturn", "Jupiter", "Mars", "Sun", "Venus", "Mercury", "Moon"};
    private static final String planetProp[]={"Saturn is the planet of karma, discipline, patience, boundaries and organization. Saturn supports long term works, career, prisons, hospitals, restraint, limitation, toughness, marriage and spell reversal.",
        "Jupiter is the planet of expansion, joviality, opportunity, intelligence and good luck. Good for increase, higher education and learning, religion, well-wishing, wealth, travel, speculation and being merciful.",
        "Mars involves enthusiasm, energy, passion, protection, aggression, determination and courage. Works related to boldness, some action, initiative, enterprise, daring, exercise and competition favours Mars.",
        "Sun is the planet of power and vitality. An all-purpose planet but does not involve secretive work. Sun supports works related to creativity, amusements, generosity, self-expression, public speaking and recognition. It gives authority, pride, success and happiness.",
        "Venus hour is good for love, luxury, beauty, decorating, shopping for romantic purpose and creativity, pleasure, harmony, pampering, diplomacy, art, music, socializing, relaxing and gardening. A planet of art and beauty in everything.",
        "Mercury is the planet of thought process and communications. This hour is good for Education, travel, thinking, communications, paper work, dealing with siblings, contracts, relatives, neighbor, health and service.",
        "Moon, the mother goddess, deals with our feminine nature, changes and emotions. Works which involves secrets, sympathy, compassion, dealing with public and generally women, dining, nurturing, growth and habitual tasks, favours Moon."
    };
    private static final String spPlanetProp[]={"Rahu symbolises immense desire. As fullfillment of one desire leads to another, it gives no satisfaction. Failures and postponement may occur due to obsession and conflict. But this time is very auspicious to perfom Durga pooja",
        "Gulika is said to be the son of Saturn. An activity during this time period will be repeated once again. Favourable works are: Building a house, buying vehicles and assets, etc.",
        "Yamakantaka is said to be the son of Jupiter. An activity started during this time period may fail at the end. It leads to abrupt ends and failures. Good time for death and death ceremonies.",
    };
    private static int tempMin=0, tempHr=0;
    private static boolean is12hrClock=false;
    private static String temp24HrStr="", temp12HrStr="";
    private static TimeZone tempTz;
    private static final Calendar tempCal=Calendar.getInstance();
    private static Date tempDate;

    public Planet(String name, long startTime, long endTime, boolean day)
    {
        this.name=name;
        this.startTime=startTime;
        this.endTime=endTime;
        this.day=day;
    }

    public void setName(String name)
    {
        this.name=name;
    }

    public String getName()
    {
        return name;
    }

    public void setStartTime(long time)
    {
        this.startTime=time;
    }

    public void setEndTime(long time)
    {
        this.endTime=time;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public void setDay(boolean day)
    {
        this.day=day;
    }

    public boolean isDay()
    {
        return day;
    }

    /**
     * Gets a hh:mm am/pm formatted String for the given hours and minutes
     * @param hr The hour(s) input
     * @param mn The minute(s) input
     * @return A String representation of the given hours and minutes
     */
    public synchronized static String getFormattedTime(int hr, int mn, boolean type12hr, boolean shortForm)
    {
        if(hr==tempHr && mn==tempMin && type12hr==is12hrClock)
        {
            if(type12hr) return temp12HrStr;
            else return temp24HrStr;
        }
        int tmp12Hr;
        if(type12hr)
        {
            if(hr >= 24)
                tmp12Hr=hr - 24;
            else if(hr >= 12)
                tmp12Hr=hr - 12;
            else
                tmp12Hr=hr;
            if(tmp12Hr == 0)
                tmp12Hr = 12;
        }
        else
        {
            tmp12Hr=hr;
        }
        StringBuilder buffer1 = new StringBuilder(8);
        if(tmp12Hr<10) buffer1.append('0');
        buffer1.append(tmp12Hr).append(':');
        if(mn<10) buffer1.append('0');
        buffer1.append(mn);
        temp24HrStr = buffer1.toString();
        if(hr>=12) buffer1.append(shortForm?" p":" PM");
        else buffer1.append(shortForm?" a":" AM");
        temp12HrStr = buffer1.toString();
        if(type12hr)
            return temp12HrStr;
        else
            return temp24HrStr;
    }

    public static String getFormattedDate(int day, int month, int year) {
        StringBuilder finalStr = new StringBuilder(12);
        finalStr.append(day);
        if (finalStr.length() == 1) {
            finalStr.insert(0, '0');
        }
        finalStr.append('-');
        switch (month) {
            case Calendar.JANUARY:
                finalStr.append("Jan");
                break;
            case Calendar.FEBRUARY:
                finalStr.append("Feb");
                break;
            case Calendar.MARCH:
                finalStr.append("Mar");
                break;
            case Calendar.APRIL:
                finalStr.append("Apr");
                break;
            case Calendar.MAY:
                finalStr.append("May");
                break;
            case Calendar.JUNE:
                finalStr.append("Jun");
                break;
            case Calendar.JULY:
                finalStr.append("Jul");
                break;
            case Calendar.AUGUST:
                finalStr.append("Aug");
                break;
            case Calendar.SEPTEMBER:
                finalStr.append("Sep");
                break;
            case Calendar.OCTOBER:
                finalStr.append("Oct");
                break;
            case Calendar.NOVEMBER:
                finalStr.append("Nov");
                break;
            case Calendar.DECEMBER:
                finalStr.append("Dec");
                break;
            default:
                finalStr.append("Jan");
                break;
        }
        finalStr.append('-');
        finalStr.append(year);
        return finalStr.toString();
    }

    public static String getFormattedTime(long milliSec, TimeZone tz, boolean type12hr, boolean shortForm)
    {
        if(tempTz==null)
        {
            tempTz = tz;
            tempCal.setTimeZone(tempTz);
        }
        else
        {
            if(!tempTz.equals(tz))
            {
                tempTz = tz;
                tempCal.setTimeZone(tempTz);
            }
        }
        if(tempDate==null) tempDate = new Date(milliSec);
        tempDate.setTime(milliSec);
        tempCal.setTime(tempDate);
        return getFormattedTime(tempCal.get(Calendar.HOUR_OF_DAY), tempCal.get(Calendar.MINUTE), type12hr, shortForm);
    }

    public static String formattedTimeString(long milliSec, TimeZone tz, boolean time12hrB, boolean shortForm)
    {
        if(tempTz==null)
        {
            tempTz = tz;
            tempCal.setTimeZone(tempTz);
        }
        else
        {
            if(!tempTz.equals(tz))
            {
                tempTz = tz;
                tempCal.setTimeZone(tempTz);
            }
        }
        if(tempDate==null) tempDate = new Date(milliSec);
        tempDate.setTime(milliSec);
        tempCal.setTime(tempDate);
        return getFormattedDate(Planet.tempCal.get(Calendar.DATE), Planet.tempCal.get(Calendar.MONTH), Planet.tempCal.get(Calendar.YEAR))+", "+getFormattedTime(tempCal.get(Calendar.HOUR_OF_DAY), tempCal.get(Calendar.MINUTE), time12hrB, shortForm);
    }

    public static String getNextPlanetTimeString(Planet nextPlanet, long curTimeMilliSec, TimeZone timez, boolean longText, boolean  ampmTime, boolean shortForm)
    {
        long npsTime, npeTime;
        long nowTime = curTimeMilliSec; //cal2.getTime().getTime();
        npsTime = nextPlanet.getStartTime();
        npeTime = nextPlanet.getEndTime();
        StringBuilder finalStr=new StringBuilder(14);
        if(npsTime > nowTime)
        {
            npsTime = npsTime - nowTime;
            int hh = Trigonometry.pureQuotient(npsTime, 3600000);
            int mm = Trigonometry.pureQuotient(npsTime % 3600000, 60000);
            finalStr.append("in ");
            if(hh==0) finalStr.append(mm);
            else finalStr.append(hh*60+ mm);
            finalStr.append(" Minutes");
            if(longText)
            {
                finalStr.append(". At ");
                finalStr.append(Planet.getFormattedTime(nextPlanet.getStartTime(), timez, ampmTime, shortForm));
                return finalStr.toString();
            }
            else
                return finalStr.toString();
        }
        else if(npeTime > nowTime && nowTime > npsTime)
        {
            finalStr.append(Planet.getFormattedTime(nextPlanet.getStartTime(), timez, ampmTime, shortForm));
            finalStr.append(ampmTime?"-":" - ");
            finalStr.append(Planet.getFormattedTime(nextPlanet.getEndTime(), timez, ampmTime, shortForm));
            return finalStr.toString();
        }
        return "";
    }

    public static String getDayName(int weekday, boolean fullName)
    {
        String dystr;
        switch(weekday)
        {
            case Calendar.SUNDAY:
                dystr = "Sunday";
                break;
            case Calendar.MONDAY:
                dystr = "Monday";
                break;
            case Calendar.TUESDAY:
                dystr = "Tuesday";
                break;
            case Calendar.WEDNESDAY:
                dystr = "Wednesday";
                break;
            case Calendar.THURSDAY:
                dystr = "Thursday";
                break;
            case Calendar.FRIDAY:
                dystr = "Friday";
                break;
            case Calendar.SATURDAY:
                dystr = "Saturday";
                break;
            default:
                dystr = "Day of Joke";
                break;
        }
        if(!fullName) dystr = new String(dystr.substring(0, 3));
        return dystr;
    }

    protected static Image getEarthSunImage(Class cls)
    {
        if (earthsun == null)
        {
            earthsun = new ImageIcon(cls.getClass().getResource("/hour/earthsun.png")).getImage();
        }
        return earthsun;
    }

    protected static Image getEarthMoonImage(Class cls)
    {
        if (earthmoon == null)
        {
            earthmoon = new ImageIcon(cls.getClass().getResource("/hour/earthmoon.png")).getImage();
        }
        return earthmoon;
    }

    protected static Image getEarthImage(Class cls)
    {
        if (earth == null)
        {
            earth = new ImageIcon(cls.getClass().getResource("/hour/earth.png")).getImage();
        }
        return earth;
    }

    protected static Image getSearchIcon(Class cls)
    {
        if (search == null)
        {
            search = new ImageIcon(cls.getClass().getResource("/hour/search.png")).getImage();
        }
        return search;
    }

    protected static Image getImageNA(Class cls)
    {
        if (NA == null)
        {
            NA = new ImageIcon(cls.getClass().getResource("/hour/na.png")).getImage();
        }
        return NA;
    }

    protected static Image getImageSun(Class cls)
    {
        if (sun == null)
        {
            sun = new ImageIcon(cls.getClass().getResource("/hour/sun.png")).getImage();
        }
        return sun;
    }

    protected static Image getImageMoon(Class cls)
    {
        if (moon == null)
        {
            moon = new ImageIcon(cls.getClass().getResource("/hour/moon.png")).getImage();
        }
        return moon;
    }

    protected static Image getImageStar(Class cls)
    {
        if (star == null)
        {
            star = new ImageIcon(cls.getClass().getResource("/hour/star.png")).getImage();
        }
        return star;
    }

    protected Image getImage(boolean isSymbol, Class cls)
    {
        if(name==null || name.equals("") || name.equals("NA")) return getImageNA(cls);
        boolean present = false;
        for(int i=0; i<chaldean.length; i++)
        {
            if(name.equalsIgnoreCase(chaldean[i]))
            {
                present = true;
                break;
            }
            else if(i<3 && name.equalsIgnoreCase(hinduSp[i]))
            {
                    present = true;
                    break;
            }
        }

        if(!present)
            return getImageNA(cls);
        else
        {
            StringBuilder img = new StringBuilder(14);
            img.append("/hour/");
            img.append(name.toLowerCase());
            if(isSymbol) img.append('2');
            //if(!selected) img.append("_1");
            img.append(".png");
            selfImage = new ImageIcon(cls.getClass().getResource(img.toString())).getImage();
            //img.delete(0, img.length());
            return selfImage;
        }
    }

    public synchronized static Image[] createImagesForList(Planet hourPlanets[], int width, int height, Planet thisLord, Planet splPlanetForThisHr, boolean symbolOn, ImageObserver obs, Class cls)
    {
        if(width <= 0 && height <= 0) width=height=32;
        else if(width <= 0 && height > 0) width=height;
        else if(width > 0 && height <= 0) height=width;
        Image newImage[] = new Image[hourPlanets.length];
        Image plImages[] = new Image[10]; // 7 chaldean and 3 spl planets
        int halfWidth = width/3;
        int imgWid = halfWidth*2;
        Image sunImg = getImageSun(cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
        Image moonImg = getImageMoon(cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
        Graphics gg;
        String pName;
        Image tmpImage;
        for (int u=0; u<hourPlanets.length; u++)
        {
            pName = hourPlanets[u].getName();
            if(pName.equals(chaldean[0]))
            {
                if(plImages[0]==null) plImages[0] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[0];
            }
            else if(pName.equals(chaldean[1]))
            {
                if(plImages[1]==null) plImages[1] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[1];
            }
            else if(pName.equals(chaldean[2]))
            {
                if(plImages[2]==null) plImages[2] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[2];
            }
            else if(pName.equals(chaldean[3]))
            {
                if(plImages[3]==null) plImages[3] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[3];
            }
            else if(pName.equals(chaldean[4]))
            {
                if(plImages[4]==null) plImages[4] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[4];
            }
            else if(pName.equals(chaldean[5]))
            {
                if(plImages[5]==null) plImages[5] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[5];
            }
            else if(pName.equals(chaldean[6]))
            {
                if(plImages[6]==null) plImages[6] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[6];
            }
            else if(pName.equals(hinduSp[0]))
            {
                if(plImages[7]==null) plImages[7] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[7];
            }
            else if(pName.equals(hinduSp[1]))
            {
                if(plImages[8]==null) plImages[8] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[8];
            }
            else if(pName.equals(hinduSp[2]))
            {
                if(plImages[9]==null) plImages[9] = hourPlanets[u].getImage(symbolOn, cls).getScaledInstance(imgWid, imgWid, Image.SCALE_FAST);
                tmpImage = plImages[9];
            }
            else
            {
                tmpImage = Planet.getImageNA(cls);
            }
            newImage[u] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            gg = newImage[u].getGraphics();
            Color blackTrans = new Color(0xff000000, true);
            gg.setColor(blackTrans);
            gg.fillRect(0, 0, width, height);
            //ImageObserver sss = new javax.swing.JLabel();
            gg.drawImage(hourPlanets[u].isDay()?sunImg:moonImg, imgWid*3/4, 0, imgWid/4, imgWid, 0, 0, newImage[u].getWidth(null), newImage[u].getHeight(null), null);
            gg.drawImage(tmpImage, halfWidth, 0, null);
        }
        return newImage;
    }

    public static String getProperty(String planetName) {
        if (planetName == null || planetName.equals("") || planetName.equals("NA")) {
            return "";
        }
        for (int i = 0; i < chaldean.length; i++) {
            if (planetName.equalsIgnoreCase(chaldean[i])) {
                return planetProp[i];
            }
        }
        for (int i = 0; i < hinduSp.length; i++) {
            if (planetName.equalsIgnoreCase(hinduSp[i])) {
                return spPlanetProp[i];
            }
        }
        return "";
    }
}
