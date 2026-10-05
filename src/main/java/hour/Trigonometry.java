/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hour;

/*
 * Trigonometry.java
 * Based on code (mMath.java) distributed freely (without license) by Richard Carless, nicknamed "xedarius" at Nokia Developer Discussion Board
 * URL: http://www.developer.nokia.com/Community/Discussion/showthread.php?72840-Maths-acos-asin-atan
 *
 */

public class Trigonometry
{
    // constants
    private static final double sq2p1 = 2.414213562373095048802e0D;
    private static final double sq2m1  = .414213562373095048802e0D;
    private static final double p4  = .161536412982230228262e2D;
    private static final double p3  = .26842548195503973794141e3D;
    private static final double p2  = .11530293515404850115428136e4D;
    private static final double p1  = .178040631643319697105464587e4D;
    private static final double p0  = .89678597403663861959987488e3D;
    private static final double q4  = .5895697050844462222791e2D;
    private static final double q3  = .536265374031215315104235e3D;
    private static final double q2  = .16667838148816337184521798e4D;
    private static final double q1  = .207933497444540981287275926e4D;
    private static final double q0  = .89678597403663861962481162e3D;
    private static final double PIO2 = 1.5707963267948966135E0D;
    private static final double nan = 0.0D/0.0D;
    private static int signum;
    private static long charac;
    private static double mantis;
    private static long finMantis;
    // reduce
    private static double mxatan(double arg)
    {
        double argsq, value;

        argsq = arg*arg;
        value = (((p4*argsq + p3)*argsq + p2)*argsq + p1)*argsq + p0;
        value = value/(((((argsq + q4)*argsq + q3)*argsq + q2)*argsq + q1)*argsq + q0);
        return value*arg;
    }

    // reduce
    private static double msatan(double arg)
    {
        if(arg < sq2m1)
            return mxatan(arg);
        if(arg > sq2p1)
            return PIO2 - mxatan(1D/arg);
            return PIO2/2D + mxatan((arg-1D)/(arg+1D));
    }

    // implementation of atan
    public static double atan(double arg)
    {
        if(arg > 0D)
            return msatan(arg);
        return -msatan(-arg);
    }

    // implementation of atan2
    public static double atan2(double arg1, double arg2)
    {
        if(arg1+arg2 == arg1)
        {
            if(arg1 >= 0D)
            return PIO2;
                return -PIO2;
        }
        arg1 = atan(arg1/arg2);
        if(arg2 < 0D)
        {
            if(arg1 <= 0D)
                return arg1 + Math.PI;
            return arg1 - Math.PI;
        }
        return arg1;
    
    }

    // implementation of asin
    public static double asin(double arg)
    {
        double temp;
        int sign;

        sign = 0;
        if(arg < 0D)
        {
            arg = -arg;
            sign++;
        }
        if(arg > 1D)
            return nan;
        temp = Math.sqrt(1D - arg*arg);
        if(arg > 0.7D)
            temp = PIO2 - atan(temp/arg);
        else
            temp = atan(arg/temp);
        if(sign > 0D)
            temp = -temp;
        return temp;
    }

    // implementation of acos
    public static double acos(double arg)
    {
        if(arg > 1D || arg < -1D)
            return nan;
        return PIO2 - asin(arg);
    }
    
    public static double sinD(double deg)
    {
            return Math.sin(Math.toRadians(deg));
    }

    public static double cosD(double deg)
    {
            return Math.cos(Math.toRadians(deg));
    }

    public static double tanD(double deg)
    {
            return Math.tan(Math.toRadians(deg));
    }

    public static double asinD(double deg)
    {
            return Math.toDegrees(asin(deg));
    }

    public static double acosD(double deg)
    {
            return Math.toDegrees(acos(deg));
    }

    public static double atanD(double deg)
    {
            return Math.toDegrees(atan(deg));
    }

    /**
     * Rounds the given double toBeConverted to the number of decimal places mentioned in numDecimals, and returns the <code>String</code> representation of the resulting double value.
     * @param toBeConverted The double value to be rounded and converted.
     * @param numDecimals Number of decimal places in the rounded value.
     * @return A <code>String</code> object representing the double toBeConverted rounded off to numDecimals decimal places.
     */
    public static String doubleToString(double toBeConverted, int numDecimals)
    {
        //preserving the signum to get the needed results from ceil/floor.
        signum = getSign(toBeConverted);
        //getting absolute value to get needed results from ceil/floor.
        toBeConverted = Math.abs(toBeConverted);
        charac = (long)Math.floor(toBeConverted);
        mantis = toBeConverted - charac;
        int decPlace = 1;
        for(int i=0; i<numDecimals; i++)
        {
            decPlace *= 10D;
        }
        mantis *= decPlace;
        finMantis = (long)Math.ceil(mantis);
        charac *= signum;
        return charac + "." + finMantis;
    }
    
    /**
     * Rounds the given double d to the number of decimal places mentioned in numDecimals.
     * @param d The double value to be rounded.
     * @param numDecimals Number of decimal places in the rounded value.
     * @return Returns the double d rounded off to numDecimals decimal places.
     */
    public static double round(double d, int numDecimals)
    {
        //preserving the signum to get the needed results from ceil/floor.
        signum = getSign(d);
        //getting absolute value to get needed results from ceil/floor.
        d = Math.abs(d);
        charac = (long)Math.floor(d);
        mantis = d - charac;
        int decPlace = 1;
        for(int i=0; i<numDecimals; i++)
        {
            decPlace *= 10D;
        }
        mantis *= decPlace;
        finMantis = (long)Math.floor(mantis);
        if((mantis - finMantis) >= 0.5D) finMantis += 1D;
        //System.out.println("signum="+signum+" charac="+charac+" finMantis="+finMantis+" decPlace="+decPlace);
        return signum * (charac + (double)finMantis/decPlace);
    }
    
    /**
     * Gets the sign of the number given as parameter.
     * @param num Is the number from which sign is to be extracted.
     * @return Returns -1 or 1 based on the sign of the parameter.
     */
    public static int getSign(double num)
    {
        if(num > 0) return 1;
        else if(num < 0) return -1;
        else return 1;
    }
    
    /**
     * Takes degree and minute as parameter and converts to decimal degree.
     * @param degree A degree value.
     * @param minute A minute value.
     * @return A double value for the decimal degree and minute.
     */
    public static double toDecimalDegrees(int degree, int minute)
    {
        return degree + minute/60D;
    }
    
    /**
     * Gets the number of milli seconds since start of day at 12 AM, given the time of day in 24 hour clock and minutes in decimals
     * @param timeOfDay A double value for time of day in "Hour.Minute" format, where Minute = Minute/60
     * @return Number of milli seconds since start of day at 12 AM (in long)
     */
    public static long getMilliSecs(double timeOfDay)
    {
	int hh = (int)Math.floor(timeOfDay);
	double mm = (timeOfDay - hh)*60D;
        //System.out.println("HH:MM = "+hh+":"+mm);
        return (long)Math.floor(((double)hh*60D+mm)*60000D);
    }
    
    /**
     * Gets the pure quotient discarding the remainder or fractions, given the dividend and divisor.
     * This method was implemented for two reasons:
     * 1. J2ME was not performing division correctly and
     * 2. Requirement of a pure quotient which may not be given by ceil, floor or round of dividend/divisor
     * @param dividend The dividend
     * @param divisor The divisor
     * @return An int value containing the quotient discarding the fraction or remainder
     */
    public static int pureQuotient(long dividend, long divisor)
    {
        int i=0;
        while(dividend >= divisor)
        {
            dividend -= divisor;
            i++;
        }
        return i;
    }
}