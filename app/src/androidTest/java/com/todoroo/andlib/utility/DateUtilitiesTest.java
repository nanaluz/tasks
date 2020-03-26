/*
 * Copyright (c) 2012 Todoroo Inc
 *
 * See the file "LICENSE" for the full license governing this code.
 */

package com.todoroo.andlib.utility;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static com.todoroo.andlib.utility.DateUtilities.getDateString;
import static com.todoroo.andlib.utility.DateUtilities.getStartOfDay;
import static com.todoroo.andlib.utility.DateUtilities.getTimeString;
import static com.todoroo.andlib.utility.DateUtilities.getWeekday;
import static com.todoroo.andlib.utility.DateUtilities.getWeekdayShort;
import static junit.framework.Assert.assertEquals;
import static org.tasks.Freeze.freezeAt;
import static org.tasks.date.DateTimeUtils.newDate;
import static org.tasks.date.DateTimeUtils.newDateTime;

import android.content.res.Configuration;
import android.util.DisplayMetrics;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tasks.Snippet;
import org.tasks.time.DateTime;

@RunWith(AndroidJUnit4.class)
public class DateUtilitiesTest {

  private static Locale defaultLocale;

  @Before
  public void setUp() {
    defaultLocale = Locale.getDefault();
    setLocale(Locale.US);
  }

  @After
  public void tearDown() {
    DateUtilities.is24HourOverride = null;
    setLocale(defaultLocale);
  }

  private void setLocale(Locale locale) {
    org.tasks.locale.Locale.setDefault(locale);
    Configuration config = new Configuration();
    config.locale = locale;
    DisplayMetrics metrics = getTargetContext().getResources().getDisplayMetrics();
    getTargetContext().getResources().updateConfiguration(config, metrics);
  }

  public void forEachLocale(Runnable r) {
    Locale[] locales = Locale.getAvailableLocales();
    for (Locale locale : locales) {
      setLocale(locale);

      r.run();
    }
  }

  @Test
  public void testTimeString() {
    forEachLocale(
        () -> {
          DateTime d = newDateTime();

          DateUtilities.is24HourOverride = false;
          for (int i = 0; i < 24; i++) {
            d = d.withHourOfDay(i);
            getTimeString(getTargetContext(), d);
          }

          DateUtilities.is24HourOverride = true;
          for (int i = 0; i < 24; i++) {
            d = d.withHourOfDay(i);
            getTimeString(getTargetContext(), d);
          }
        });
  }

  @Test
  public void testDateString() {
    forEachLocale(
        () -> {
          DateTime d = newDateTime();

          for (int i = 0; i < 12; i++) {
            d = d.withMonthOfYear(i);
            getDateString(d);
          }
        });
  }

  @Test
  public void testGet24HourTime() {
    DateUtilities.is24HourOverride = true;
    assertEquals("09:05", getTimeString(null, new DateTime(2014, 1, 4, 9, 5, 36)));
    assertEquals("13:00", getTimeString(null, new DateTime(2014, 1, 4, 13, 0, 1)));
  }

  @Test
  public void testGetTime() {
    DateUtilities.is24HourOverride = false;
    assertEquals("9:05 AM", getTimeString(null, new DateTime(2014, 1, 4, 9, 5, 36)));
    assertEquals("1:05 PM", getTimeString(null, new DateTime(2014, 1, 4, 13, 5, 36)));
  }

  @Test
  public void testGetTimeWithNoMinutes() {
    DateUtilities.is24HourOverride = false;
    assertEquals("1 PM", getTimeString(null, new DateTime(2014, 1, 4, 13, 0, 59))); // derp?
  }

  @Test
  public void testGetDateStringWithYear() {
    assertEquals("Jan 4 '14", getDateString(new DateTime(2014, 1, 4, 0, 0, 0)));
  }

  @Test
  public void testGetDateStringHidingYear() {
    freezeAt(newDate(2014, 1, 1))
        .thawAfter(
            new Snippet() {
              {
                assertEquals("Jan 1", getDateString(newDateTime()));
              }
            });
  }

  @Test
  public void testGetDateStringWithDifferentYear() {
    freezeAt(newDate(2013, 12, 31))
        .thawAfter(
            new Snippet() {
              {
                assertEquals("Jan 1 '14", getDateString(new DateTime(2014, 1, 1, 0, 0, 0)));
              }
            });
  }

  @Test
  public void testShouldGetStartOfDay() {
    DateTime now = new DateTime(2014, 1, 3, 10, 41, 41, 520);
    assertEquals(now.startOfDay().getMillis(), getStartOfDay(now.getMillis()));
  }

  @Test
  public void testGetWeekdayLongString() {
    assertEquals("Sunday", getWeekday(newDate(2013, 12, 29)));
    assertEquals("Monday", getWeekday(newDate(2013, 12, 30)));
    assertEquals("Tuesday", getWeekday(newDate(2013, 12, 31)));
    assertEquals("Wednesday", getWeekday(newDate(2014, 1, 1)));
    assertEquals("Thursday", getWeekday(newDate(2014, 1, 2)));
    assertEquals("Friday", getWeekday(newDate(2014, 1, 3)));
    assertEquals("Saturday", getWeekday(newDate(2014, 1, 4)));
  }

  @Test
  public void testGetWeekdayShortString() {
    assertEquals("Sun", getWeekdayShort(newDate(2013, 12, 29)));
    assertEquals("Mon", getWeekdayShort(newDate(2013, 12, 30)));
    assertEquals("Tue", getWeekdayShort(newDate(2013, 12, 31)));
    assertEquals("Wed", getWeekdayShort(newDate(2014, 1, 1)));
    assertEquals("Thu", getWeekdayShort(newDate(2014, 1, 2)));
    assertEquals("Fri", getWeekdayShort(newDate(2014, 1, 3)));
    assertEquals("Sat", getWeekdayShort(newDate(2014, 1, 4)));
  }

  @Test
  public void usDateNoYear() {
    setLocale(Locale.US);
    freezeAt(new DateTime(2018, 1, 1))
        .thawAfter(
            () ->
                assertEquals(
                    "Jan 14",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void usDateWithYear() {
    setLocale(Locale.US);
    freezeAt(new DateTime(2017, 12, 12))
        .thawAfter(
            () ->
                assertEquals(
                    "Jan 14 '18",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void germanDateNoYear() {
    setLocale(Locale.GERMAN);
    freezeAt(new DateTime(2018, 1, 1))
        .thawAfter(
            () ->
                assertEquals(
                    "14 Jan.",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void germanDateWithYear() {
    setLocale(Locale.GERMAN);
    freezeAt(new DateTime(2017, 12, 12))
        .thawAfter(
            () ->
                assertEquals(
                    "14 Jan. '18",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void koreanDateNoYear() {
    setLocale(Locale.KOREAN);
    freezeAt(new DateTime(2018, 1, 1))
        .thawAfter(
            () ->
                assertEquals(
                    "1월 14일",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void koreanDateWithYear() {
    setLocale(Locale.KOREAN);
    freezeAt(new DateTime(2017, 12, 12))
        .thawAfter(
            () ->
                assertEquals(
                    "18년 1월 14일",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void japaneseDateNoYear() {
    setLocale(Locale.JAPANESE);
    freezeAt(new DateTime(2018, 1, 1))
        .thawAfter(
            () ->
                assertEquals(
                    "1月 14日",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void japaneseDateWithYear() {
    setLocale(Locale.JAPANESE);
    freezeAt(new DateTime(2017, 12, 12))
        .thawAfter(
            () ->
                assertEquals(
                    "18年 1月 14日",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void chineseDateNoYear() {
    setLocale(Locale.CHINESE);
    freezeAt(new DateTime(2018, 1, 1))
        .thawAfter(
            () ->
                assertEquals(
                    "1月 14日",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }

  @Test
  public void chineseDateWithYear() {
    setLocale(Locale.CHINESE);
    freezeAt(new DateTime(2017, 12, 12))
        .thawAfter(
            () ->
                assertEquals(
                    "18年 1月 14日",
                    DateUtilities.getRelativeDateStringWithTime(
                        getTargetContext(), new DateTime(2018, 1, 14).getMillis())));
  }
}
