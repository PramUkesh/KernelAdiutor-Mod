/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.utils.kernel.battery;

import android.content.Context;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 26.06.16.
 */
public class Battery {

    private static final String FORCE_FAST_CHARGE = "/sys/kernel/fast_charge/force_fast_charge";
    private static final String BLX = "/sys/devices/virtual/misc/batterylifeextender/charging_limit";

    private static final String CHARGE_RATE = "/sys/kernel/Quick_Charge";
    private static final String CHARGE_RATE_ENABLE = CHARGE_RATE + "/QC_Toggle";
    private static final String CUSTOM_CURRENT = CHARGE_RATE + "/custom_current";
    private static final String FC_SWITCH = "/sys/kernel/Fast_Charge/FC_Switch";
    private static final String FASTCHG_CURRENT = "/sys/kernel/Fast_Charge/custom_current";
    private static final String DYNAMIC_CURRENT = "sys/class/power_supply/battery/current_now";
    private static final String CHARGING = "sys/class/power_supply/battery/status";
    private static final String USB_CUSTOM_CURRENT = CHARGE_RATE + "/USB_Current";
    private static final String CHARGE_PROFILE = CHARGE_RATE + "/Charging_Profile"; 

    private static Integer sCapacity;
    
    public static void setfastchgCurrent(int value, Context context) {
        run(Control.write(String.valueOf(value), FASTCHG_CURRENT), FASTCHG_CURRENT, context);
    }

    public static int getfastchgCurrent() {
        return Utils.strToInt(Utils.readFile(FASTCHG_CURRENT));
    }

    public static boolean hasfastchgCurrent() {
        return Utils.existFile(FASTCHG_CURRENT);
    }

    public static void setChargingCurrent(int value, Context context) {
        run(Control.write(String.valueOf(value), CUSTOM_CURRENT), CUSTOM_CURRENT, context);
    }

    public static int getChargingCurrent() {
        return Utils.strToInt(Utils.readFile(CUSTOM_CURRENT));
    }

    public static boolean hasChargingCurrent() {
        return Utils.existFile(CUSTOM_CURRENT);
    }
    
    public static void setUSBChargingCurrent(int value, Context context) {
        run(Control.write(String.valueOf(value), USB_CUSTOM_CURRENT), USB_CUSTOM_CURRENT, context);
    }

    public static int getUSBChargingCurrent() {
        return Utils.strToInt(Utils.readFile(USB_CUSTOM_CURRENT));
    }

    public static boolean hasUSBChargingCurrent() {
        return Utils.existFile(USB_CUSTOM_CURRENT);
    }
    
    public static boolean hasDc() {
        return Utils.existFile(DYNAMIC_CURRENT);
    } 
    
    public static int getDc() {
        return Utils.strToInt(Utils.readFile(DYNAMIC_CURRENT));
    }
    
    public static boolean isCharge() {
        return Utils.readFile(CHARGING).equals("Discharging");
    }
    
   public static boolean hasChargeProfile() {
        return Utils.existFile(CHARGE_PROFILE);
    }

    public static int getProfiles() {
        String file = CHARGE_PROFILE;
        return Utils.strToInt(Utils.readFile(file));
    }
    
    public static boolean isThunder() {
        return Utils.readFile(CHARGE_PROFILE).equals("2");
    }

    public static List<String> getProfilesMenu(Context context) {
        List<String> list = new ArrayList<>();
        list.add(context.getString(R.string.balancedcharge));
        list.add(context.getString(R.string.fastcharge));
        list.add(context.getString(R.string.thundercharge));
        return list;
    }
    
    public static void setchargeProfile(int value, Context context) {
        String file = CHARGE_PROFILE;
        run(Control.write(String.valueOf(value), file), file, context);
    }

    public static void enableChargeRate(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", CHARGE_RATE_ENABLE), CHARGE_RATE_ENABLE, context);
    }

    public static boolean isChargeRateEnabled() {
        return Utils.readFile(CHARGE_RATE_ENABLE).equals("1");
    }

    public static boolean hasChargeRateEnable() {
        return Utils.existFile(CHARGE_RATE_ENABLE);
    }
    public static void enableFC(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", FC_SWITCH), FC_SWITCH, context);
    }

    public static boolean isFCEnabled() {
        return Utils.readFile(FC_SWITCH).equals("1");
    }

    public static boolean hasFCEnable() {
        return Utils.existFile(FC_SWITCH);
    }

    public static void setBlx(int value, Context context) {
        run(Control.write(String.valueOf(value == 0 ? 101 : value - 1), BLX), BLX, context);
    }

    public static int getBlx() {
        int value = Utils.strToInt(Utils.readFile(BLX));
        return value > 100 ? 0 : value + 1;
    }

    public static boolean hasBlx() {
        return Utils.existFile(BLX);
    }

    public static void enableForceFastCharge(boolean enable, Context context) {
        run(Control.write(enable ? "1" : "0", FORCE_FAST_CHARGE), FORCE_FAST_CHARGE, context);
    }

    public static boolean isForceFastChargeEnabled() {
        return Utils.readFile(FORCE_FAST_CHARGE).equals("1");
    }

    public static boolean hasForceFastCharge() {
        return Utils.existFile(FORCE_FAST_CHARGE);
    }

    public static int getCapacity(Context context) {
        if (sCapacity == null) {
            try {
                Class<?> powerProfile = Class.forName("com.android.internal.os.PowerProfile");
                Constructor constructor = powerProfile.getDeclaredConstructor(Context.class);
                Object powerProInstance = constructor.newInstance(context);
                Method batteryCap = powerProfile.getMethod("getBatteryCapacity");
                sCapacity = Math.round((long) (double) batteryCap.invoke(powerProInstance));
            } catch (Exception e) {
                e.printStackTrace();
                sCapacity = 0;
            }
        }
        return sCapacity;
    }

    public static boolean hasCapacity(Context context) {
        return getCapacity(context) != 0;
    }

    public static boolean supported(Context context) {
        return hasCapacity(context);
    }

    private static void run(String command, String id, Context context) {
        Control.runSetting(command, ApplyOnBootFragment.BATTERY, id, context);
    }

}
