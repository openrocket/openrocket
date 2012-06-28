/**
 * Copyright (C) 2009 - 2012 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chart;

import java.io.Serializable;

/**
 * Holds An PieChart Segment
 */
public class PieSegment implements Serializable {
  private float mStartAngle;

  private float mEndAngle;

  private int mDataIndex;

  private float mValue;

  public PieSegment(int dataIndex, float value, float startAngle, float angle) {
    mStartAngle = startAngle;
    mEndAngle = angle + startAngle;
    mDataIndex = dataIndex;
    mValue = value;
  }

  /**
   * Checks if angle falls in segment.
   * 
   * @param angle
   * @return true if in segment, false otherwise.
   */
  public boolean isInSegment(double angle) {
    return angle >= mStartAngle && angle <= mEndAngle;
  }

  protected float getStartAngle() {
    return mStartAngle;
  }

  protected float getEndAngle() {
    return mEndAngle;
  }

  protected int getDataIndex() {
    return mDataIndex;
  }

  protected float getValue() {
    return mValue;
  }

  public String toString() {
    return "mDataIndex=" + mDataIndex + ",mValue=" + mValue + ",mStartAngle=" + mStartAngle
        + ",mEndAngle=" + mEndAngle;
  }

}
