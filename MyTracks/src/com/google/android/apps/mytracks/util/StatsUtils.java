/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.apps.mytracks.util;

import com.google.android.apps.mytracks.content.MyTracksProviderUtils;
import com.google.android.apps.mytracks.content.Track;
import com.google.android.apps.mytracks.stats.TripStatistics;
import com.google.android.apps.mytracks.util.CalorieUtils.ActivityType;
import com.google.android.maps.mytracks.R;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

/**
 * Utilities for updating the statistics UI labels and values.
 * 
 * @author Jimmy Shih
 */
public class StatsUtils {

  private static final String COORDINATE_DEGREE = "\u00B0";
  private static final String GRADE_PERCENTAGE = "%";

  private static final String ELEVATION_FORMAT = "%1$.2f";
  private static final String GRADE_FORMAT = "%1$d";
  private static final String CALORIES_FORMAT = "%1$.0f";

  private StatsUtils() {}

  /**
   * Sets the location values.
   * 
   * @param context the context
   * @param activity the activity for finding views. If null, the view cannot be
   *          null
   * @param view the containing view for finding views. If null, the activity
   *          cannot be null
   * @param location the location
   * @param isRecording true if recording
   */
  public static void setLocationValues(
      Context context, Activity activity, View view, Location location, boolean isRecording) {
    boolean metricUnits = PreferencesUtils.isMetricUnits(context);
    boolean reportSpeed = PreferencesUtils.isReportSpeed(context);

    // Set speed/pace
    double speed = isRecording && location != null && location.hasSpeed() ? location.getSpeed()
        : Double.NaN;
    setSpeed(context, getView(activity, view, R.id.stats_speed), R.string.stats_speed,
        R.string.stats_pace, speed, metricUnits, reportSpeed);

    // Set elevation
    boolean showGradeElevation = PreferencesUtils.getBoolean(context,
        R.string.stats_show_grade_elevation_key,
        PreferencesUtils.STATS_SHOW_GRADE_ELEVATION_DEFAULT);
    View elevation = getView(activity, view, R.id.stats_elevation);

    if (showGradeElevation && isRecording) {
      double altitude = location != null && location.hasAltitude() ? location.getAltitude()
          : Double.NaN;
      elevation.setVisibility(View.VISIBLE);
      setElevationValue(context, elevation, -1, altitude, metricUnits);
    } else {
      elevation.setVisibility(View.GONE);
    }

    // Set coordinate
    boolean showCoordinate = PreferencesUtils.getBoolean(context,
        R.string.stats_show_coordinate_key, PreferencesUtils.STATS_SHOW_COORDINATE_DEFAULT);
    View coordinateHorizontalLine = getView(activity, view, R.id.stats_coordinate_horizontal_line);
    View coordinateContainer = getView(activity, view, R.id.stats_coordinate_container);

    if (showCoordinate && isRecording) {
      double latitude = location != null ? location.getLatitude() : Double.NaN;
      double longitude = location != null ? location.getLongitude() : Double.NaN;
      coordinateHorizontalLine.setVisibility(View.VISIBLE);
      coordinateContainer.setVisibility(View.VISIBLE);
      setCoordinateValue(
          context, getView(activity, view, R.id.stats_latitude), R.string.stats_latitude, latitude);
      setCoordinateValue(context, getView(activity, view, R.id.stats_longitude),
          R.string.stats_longitude, longitude);
    } else {
      coordinateHorizontalLine.setVisibility(View.GONE);
      coordinateContainer.setVisibility(View.GONE);
    }
  }

  /**
   * Sets the total time value.
   * 
   * @param activity the activity
   * @param totalTime the total time
   */
  public static void setTotalTimeValue(Activity activity, long totalTime) {
    setTimeValue(activity, activity.findViewById(R.id.stats_total_time), R.string.stats_total_time,
        totalTime);
  }

  /**
   * Sets the trip statistics values.
   * 
   * @param context the context
   * @param activity the activity for finding views. If null, then view cannot
   *          be null
   * @param view the containing view for finding views. If null, the activity
   *          cannot be null
   * @param tripStatistics the trip statistics
   * @param trackId the id of track, which is used to set calorie value. Does
   *          not handle the calorie If the value is
   *          {@link PreferencesUtils#RECORDING_TRACK_ID_DEFAULT}
   */
  public static void setTripStatisticsValues(
      Context context, Activity activity, View view, TripStatistics tripStatistics, long trackId) {
    boolean metricUnits = PreferencesUtils.isMetricUnits(context);
    boolean reportSpeed = PreferencesUtils.isReportSpeed(context);

    // Set total distance
    double totalDistance = tripStatistics == null ? Double.NaN : tripStatistics.getTotalDistance();
    setDistanceValue(
        context, getView(activity, view, R.id.stats_distance), totalDistance, metricUnits);

    // Set total time
    setTimeValue(context, getView(activity, view, R.id.stats_total_time), R.string.stats_total_time,
        tripStatistics != null ? tripStatistics.getTotalTime() : -1L);

    // Set average speed/pace
    double averageSpeed = tripStatistics != null ? tripStatistics.getAverageSpeed() : Double.NaN;
    setSpeed(context, getView(activity, view, R.id.stats_average_speed),
        R.string.stats_average_speed, R.string.stats_average_pace, averageSpeed, metricUnits,
        reportSpeed);

    // Set moving time
    setTimeValue(context, getView(activity, view, R.id.stats_moving_time),
        R.string.stats_moving_time, tripStatistics != null ? tripStatistics.getMovingTime() : -1L);

    // Set average moving speed/pace
    double averageMovingSpeed = tripStatistics != null ? tripStatistics.getAverageMovingSpeed()
        : Double.NaN;
    setSpeed(context, getView(activity, view, R.id.stats_average_moving_time),
        R.string.stats_average_moving_speed, R.string.stats_average_moving_pace, averageMovingSpeed,
        metricUnits, reportSpeed);

    // Set max speed/pace
    double maxSpeed = tripStatistics == null ? Double.NaN : tripStatistics.getMaxSpeed();
    setSpeed(context, getView(activity, view, R.id.stats_max_speed), R.string.stats_max_speed,
        R.string.stats_fastest_pace, maxSpeed, metricUnits, reportSpeed);

    // Set grade/elevation
    boolean showGradeElevation = PreferencesUtils.getBoolean(context,
        R.string.stats_show_grade_elevation_key,
        PreferencesUtils.STATS_SHOW_GRADE_ELEVATION_DEFAULT);
    View gradeElevationHorizontalLine = getView(
        activity, view, R.id.stats_grade_elevation_horizontal_line);
    View gradeElevationContainer = getView(activity, view, R.id.stats_grade_elevation_container);

    if (showGradeElevation) {
      gradeElevationHorizontalLine.setVisibility(View.VISIBLE);
      gradeElevationContainer.setVisibility(View.VISIBLE);
      // Set grade
      double minGrade = tripStatistics == null ? Double.NaN : tripStatistics.getMinGrade();
      double maxGrade = tripStatistics == null ? Double.NaN : tripStatistics.getMaxGrade();
      setGradeValue(
          context, getView(activity, view, R.id.stats_grade_min), R.string.stats_min, minGrade);
      setGradeValue(
          context, getView(activity, view, R.id.stats_grade_max), R.string.stats_max, maxGrade);

      // Set elevation
      double elevationGain = tripStatistics == null ? Double.NaN
          : tripStatistics.getTotalElevationGain();
      double minElevation = tripStatistics == null ? Double.NaN : tripStatistics.getMinElevation();
      double maxElevation = tripStatistics == null ? Double.NaN : tripStatistics.getMaxElevation();
      setElevationValue(context, getView(activity, view, R.id.stats_elevation_gain),
          R.string.stats_gain, elevationGain, metricUnits);
      setElevationValue(context, getView(activity, view, R.id.stats_elevation_min),
          R.string.stats_min, minElevation, metricUnits);
      setElevationValue(context, getView(activity, view, R.id.stats_elevation_max),
          R.string.stats_max, maxElevation, metricUnits);
    } else {
      gradeElevationHorizontalLine.setVisibility(View.GONE);
      gradeElevationContainer.setVisibility(View.GONE);
    }

    if (trackId != PreferencesUtils.RECORDING_TRACK_ID_DEFAULT) {
      // Set calories
      boolean showCalorie = PreferencesUtils.getBoolean(
          context, R.string.stats_show_calorie_key, PreferencesUtils.STATS_SHOW_CALORIE_DEFAULT);
      View calorieHorizontalLine = getView(activity, view, R.id.stats_calorie_horizontal_line);
      View calorieContainer = getView(activity, view, R.id.stats_calorie_container);
      if (showCalorie) {
        calorieHorizontalLine.setVisibility(View.VISIBLE);
        calorieContainer.setVisibility(View.VISIBLE);
        double calories = Double.NaN;
        if (tripStatistics != null) {
          calories = tripStatistics.getCalorie();
        }
        MyTracksProviderUtils myTracksProviderUtils = MyTracksProviderUtils.Factory.get(context);
        Track track = myTracksProviderUtils.getTrack(trackId);
        ActivityType activityType = track == null ? ActivityType.INVALID
            : CalorieUtils.getActivityType(context, track.getCategory());
        setCalorie(context, getView(activity, view, R.id.stats_calorie), activityType, calories);
      } else {
        calorieHorizontalLine.setVisibility(View.GONE);
        calorieContainer.setVisibility(View.GONE);
      }
    }
  }

  /**
   * Sets speed.
   * 
   * @param context the context
   * @param view the containing view
   * @param speedLabelId the speed label id
   * @param paceLabelId the pace label id
   * @param speed the speed in meters per second
   * @param metricUnits true if metric units
   * @param reportSpeed true if report speed
   */
  private static void setSpeed(Context context, View view, int speedLabelId, int paceLabelId,
      double speed, boolean metricUnits, boolean reportSpeed) {
    String parts[] = StringUtils.getSpeedParts(context, speed, metricUnits, reportSpeed);
    setItem(context, view, reportSpeed ? speedLabelId : paceLabelId, parts[0], parts[1]);
  }

  /**
   * Sets distance value.
   * 
   * @param context the context
   * @param view the containing view
   * @param distance the distance in meters
   * @param metricUnits true if metric units
   */
  private static void setDistanceValue(
      Context context, View view, double distance, boolean metricUnits) {
    String parts[] = StringUtils.getDistanceParts(context, distance, metricUnits);
    setItem(context, view, R.string.stats_distance, parts[0], parts[1]);
  }

  /**
   * Sets a time value.
   * 
   * @param context the context
   * @param view the containing view
   * @param labelId the label id
   * @param time the time
   */
  private static void setTimeValue(Context context, View view, int labelId, long time) {
    String value = time == -1L ? null : StringUtils.formatElapsedTime(time);
    setItem(context, view, labelId, value, null);
  }

  /**
   * Sets an elevation value.
   * 
   * @param context the context
   * @param view the containing view
   * @param labelId the label id
   * @param elevation the elevation in meters
   * @param metricUnits true if metric units
   */
  private static void setElevationValue(
      Context context, View view, int labelId, double elevation, boolean metricUnits) {
    String value;
    String unit;
    if (Double.isNaN(elevation) || Double.isInfinite(elevation)) {
      value = null;
      unit = null;
    } else {
      if (metricUnits) {
        value = String.format(Locale.getDefault(), ELEVATION_FORMAT, elevation);
        unit = context.getString(R.string.unit_meter);
      } else {
        elevation *= UnitConversions.M_TO_FT;
        value = String.format(Locale.getDefault(), ELEVATION_FORMAT, elevation);
        unit = context.getString(R.string.unit_feet);
      }
    }
    setItem(context, view, labelId, value, unit);
  }

  /**
   * Sets a grade value.
   * 
   * @param context the context
   * @param view the containing view
   * @param labelId the label id
   * @param grade the grade in fraction between 0 and 1
   */
  private static void setGradeValue(Context context, View view, int labelId, double grade) {
    String value = Double.isNaN(grade) || Double.isInfinite(grade) ? null
        : String.format(Locale.getDefault(), GRADE_FORMAT, Math.round(grade * 100));
    setItem(context, view, labelId, value, GRADE_PERCENTAGE);
  }

  /**
   * Sets a coordinate value.
   * 
   * @param context the context
   * @param view the containing view
   * @param labelId the label id
   * @param coordinate the coordinate in degrees
   */
  private static void setCoordinateValue(
      Context context, View view, int labelId, double coordinate) {
    String value = Double.isNaN(coordinate) || Double.isInfinite(coordinate) ? null
        : Location.convert(coordinate, Location.FORMAT_DEGREES);
    setItem(context, view, labelId, value, COORDINATE_DEGREE);
  }

  /**
   * Sets calorie.
   * 
   * @param context the context
   * @param view the containing view
   * @param activityType the activity type
   * @param calorie the value of calorie
   */
  private static void setCalorie(
      Context context, View view, ActivityType activityType, double calorie) {
    if (activityType == ActivityType.INVALID) {
      view.setVisibility(View.GONE);
    } else {
      view.setVisibility(View.VISIBLE);
      setItem(context, view, R.string.stats_calorie,
          String.format(Locale.getDefault(), CALORIES_FORMAT, calorie),
          context.getString(R.string.unit_calorie));
    }
  }

  /**
   * Sets an item.
   * 
   * @param context the context
   * @param view the containing view
   * @param labelId the label id. -1 to hide the label
   * @param value the value, can be null
   * @param unit the unit. Null to hide the unit
   */
  private static void setItem(Context context, View view, int labelId, String value, String unit) {
    TextView labelTextView = (TextView) view.findViewById(R.id.stats_label);
    TextView valueTextView = (TextView) view.findViewById(R.id.stats_value);
    TextView unitTextView = (TextView) view.findViewById(R.id.stats_unit);
    if (labelTextView == null || valueTextView == null || unitTextView == null) {
      return;
    }
    if (labelId == -1) {
      labelTextView.setVisibility(View.GONE);
    } else {
      labelTextView.setVisibility(View.VISIBLE);
      labelTextView.setText(labelId);
    }

    if (value == null) {
      value = context.getString(R.string.value_unknown);
      unitTextView.setVisibility(View.GONE);
    } else {
      if (unit == null) {
        unitTextView.setVisibility(View.GONE);
      } else {
        unitTextView.setVisibility(View.VISIBLE);
        unitTextView.setText(unit);
      }
    }
    valueTextView.setText(value);
  }

  /**
   * Get a view.
   * 
   * @param activity the activity
   * @param view the containing view
   * @param id the id
   */
  private static View getView(Activity activity, View view, int id) {
    if (activity != null) {
      return activity.findViewById(id);
    } else {
      return view.findViewById(id);
    }
  }
}