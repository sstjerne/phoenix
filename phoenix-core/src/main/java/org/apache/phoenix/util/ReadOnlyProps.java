/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.phoenix.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * 
 * Read-only properties that avoids unnecessary synchronization in
 * java.util.Properties.
 *
 * 
 * @since 1.2.2
 */
public class ReadOnlyProps implements Iterable<Entry<String, String>> {
    public static final ReadOnlyProps EMPTY_PROPS = new ReadOnlyProps();
    private final Map<String, String> props;
    
    private ConfigReader configReader = new ConfigReader();
    
    public ReadOnlyProps(ReadOnlyProps defaultProps, Iterator<Entry<String, String>> iterator) {
        Map<String, String> map = new HashMap<String,String>(defaultProps.asMap());
        while (iterator.hasNext()) {
            Entry<String,String> entry = iterator.next();
            map.put(entry.getKey(), entry.getValue());
        }
        this.props = ImmutableMap.copyOf(map);
    }

    public ReadOnlyProps(Iterator<Entry<String, String>> iterator) {
        this(EMPTY_PROPS, iterator);
    }

    private ReadOnlyProps() {
        this.props = Collections.emptyMap();
    }

    public ReadOnlyProps(Map<String, String> props) {
    	
    	//MODIFIED READ ONLY PROPS CODE
	
    	Map<String, String> newProps = new HashMap<String,String>();
    	
    	String hbaseZkPropClientPort = configReader.getValue("hbase.zookeeper.property.clientPort");
    	String hbaseZkQuorum = configReader.getValue("hbase.zookeeper.quorum");
    	String zkZnodeParent = configReader.getValue("zookeeper.znode.parent");
    	String hbaseMaster = configReader.getValue("hbase.master");
    	String phoenixZkQuorum= configReader.getValue("phoenix.zookeeper.quorum");
    	String hbaseRpcTimeout= configReader.getValue("hbase.rpc.timeout");
    	String hbaseClientScannerTimeoutPeriod= configReader.getValue("hbase.client.scanner.timeout.period");
    	String phoenixQueryTargetConcurrency= configReader.getValue("phoenix.query.targetConcurrency");
    	String phoenixQueryMaxConcurrency= configReader.getValue("phoenix.query.maxConcurrency");
    	String phoenixQueryMaxIntraRegionParallelization= configReader.getValue("phoenix.query.maxIntraRegionParallelization");
    	String hbaseClientScannerCaching= configReader.getValue("hbase.client.scanner.caching");
    	String phoenixQueryTimeoutMs= configReader.getValue("phoenix.query.timeoutMs");
    	
    	newProps.put("hbase.zookeeper.property.clientPort",hbaseZkPropClientPort);
        newProps.put("hbase.zookeeper.quorum", hbaseZkQuorum);
        newProps.put("zookeeper.znode.parent",zkZnodeParent);
        newProps.put("timeout", "240000");
        newProps.put("hbase.master", hbaseMaster);
        newProps.put("phoenix.zookeeper.quorum", phoenixZkQuorum);
        newProps.put("hbase.rpc.timeout", hbaseRpcTimeout);
        newProps.put("hbase.client.scanner.timeout.period", hbaseClientScannerTimeoutPeriod);
        newProps.put("phoenix.query.targetConcurrency", phoenixQueryTargetConcurrency);
        newProps.put("phoenix.query.maxConcurrency", phoenixQueryMaxConcurrency);
        newProps.put("phoenix.query.maxIntraRegionParallelization", phoenixQueryMaxIntraRegionParallelization);
        newProps.put("hbase.client.scanner.caching", hbaseClientScannerCaching);
        newProps.put("phoenix.query.timeoutMs",phoenixQueryTimeoutMs);
        
       /*
    	newProps.put("hbase.zookeeper.property.clientPort", "2181");
        newProps.put("hbase.zookeeper.quorum", "10.10.0.15");
        newProps.put("zookeeper.znode.parent","/hbase");
        newProps.put("timeout", "240000");
        newProps.put("hbase.master", "*10.10.0.15:60000*");
        newProps.put("phoenix.zookeeper.quorum", "10.10.0.15");
        newProps.put("hbase.rpc.timeout", "120000000");
        newProps.put("hbase.client.scanner.timeout.period", "1200000");
        newProps.put("phoenix.query.targetConcurrency", "32");
        newProps.put("phoenix.query.maxConcurrency", "64");
        newProps.put("phoenix.query.maxIntraRegionParallelization", "1");
        newProps.put("hbase.client.scanner.caching", "10000");
        newProps.put("phoenix.query.timeoutMs","18000000");
        */

        System.out.println("jude -> " + newProps);
        Map<String, String> tmpAugmentedProps = Maps.newHashMapWithExpectedSize(newProps.size() + props.size());

        tmpAugmentedProps.putAll(newProps);
        tmpAugmentedProps.putAll((Map)props);

        this.props = ImmutableMap.copyOf(tmpAugmentedProps);

    }

    private static Pattern varPat = Pattern.compile("\\$\\{[^\\}\\$\u0020]+\\}");
    private static int MAX_SUBST = 20;

    private String substituteVars(String expr) {
        if (expr == null) {
          return null;
        }
        Matcher match = varPat.matcher("");
        String eval = expr;
        for(int s=0; s<MAX_SUBST; s++) {
          match.reset(eval);
          if (!match.find()) {
            return eval;
          }
          String var = match.group();
          var = var.substring(2, var.length()-1); // remove ${ .. }
          String val = null;
          try {
            val = System.getProperty(var);
          } catch(SecurityException se) {
          }
          if (val == null) {
            val = getRaw(var);
          }
          if (val == null) {
            return eval; // return literal ${var}: var is unbound
          }
          // substitute
          eval = eval.substring(0, match.start())+val+eval.substring(match.end());
        }
        throw new IllegalStateException("Variable substitution depth too large: " 
                                        + MAX_SUBST + " " + expr);
      }
      
    /**
     * Get the value of the <code>name</code> property, without doing
     * <a href="#VariableExpansion">variable expansion</a>.
     * 
     * @param name the property name.
     * @return the value of the <code>name</code> property, 
     *         or null if no such property exists.
     */
    public String getRaw(String name) {
      return props.get(name);
    }

    public String getRaw(String name, String defaultValue) {
        String value = getRaw(name);
        if (value == null) {
            return defaultValue;
        }
        return value;
      }

    /** 
     * Get the value of the <code>name</code> property. If no such property 
     * exists, then <code>defaultValue</code> is returned.
     * 
     * @param name property name.
     * @param defaultValue default value.
     * @return property value, or <code>defaultValue</code> if the property 
     *         doesn't exist.                    
     */
    public String get(String name, String defaultValue) {
      return substituteVars(getRaw(name, defaultValue));
    }
      
    /**
     * Get the value of the <code>name</code> property, <code>null</code> if
     * no such property exists.
     * 
     * Values are processed for <a href="#VariableExpansion">variable expansion</a> 
     * before being returned. 
     * 
     * @param name the property name.
     * @return the value of the <code>name</code> property, 
     *         or null if no such property exists.
     */
    public String get(String name) {
      return substituteVars(getRaw(name));
    }

    private String getHexDigits(String value) {
        boolean negative = false;
        String str = value;
        String hexString = null;
        if (value.startsWith("-")) {
          negative = true;
          str = value.substring(1);
        }
        if (str.startsWith("0x") || str.startsWith("0X")) {
          hexString = str.substring(2);
          if (negative) {
            hexString = "-" + hexString;
          }
          return hexString;
        }
        return null;
      }
      
    /** 
     * Get the value of the <code>name</code> property as a <code>boolean</code>.  
     * If no such property is specified, or if the specified value is not a valid
     * <code>boolean</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name property name.
     * @param defaultValue default value.
     * @return property value as a <code>boolean</code>, 
     *         or <code>defaultValue</code>. 
     */
    public boolean getBoolean(String name, boolean defaultValue) {
      String valueString = get(name);
      if ("true".equals(valueString))
        return true;
      else if ("false".equals(valueString))
        return false;
      else return defaultValue;
    }

    /** 
     * Get the value of the <code>name</code> property as an <code>int</code>.
     *   
     * If no such property exists, or if the specified value is not a valid
     * <code>int</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name property name.
     * @param defaultValue default value.
     * @return property value as an <code>int</code>, 
     *         or <code>defaultValue</code>. 
     */
    public int getInt(String name, int defaultValue) {
      String valueString = get(name);
      if (valueString == null)
        return defaultValue;
      try {
        String hexString = getHexDigits(valueString);
        if (hexString != null) {
          return Integer.parseInt(hexString, 16);
        }
        return Integer.parseInt(valueString);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }
    
    /** 
     * Get the value of the <code>name</code> property as a <code>long</code>.  
     * If no such property is specified, or if the specified value is not a valid
     * <code>long</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name property name.
     * @param defaultValue default value.
     * @return property value as a <code>long</code>, 
     *         or <code>defaultValue</code>. 
     */
    public long getLong(String name, long defaultValue) {
      String valueString = get(name);
      if (valueString == null)
        return defaultValue;
      try {
        String hexString = getHexDigits(valueString);
        if (hexString != null) {
          return Long.parseLong(hexString, 16);
        }
        return Long.parseLong(valueString);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }

    /** 
     * Get the value of the <code>name</code> property as a <code>float</code>.  
     * If no such property is specified, or if the specified value is not a valid
     * <code>float</code>, then <code>defaultValue</code> is returned.
     * 
     * @param name property name.
     * @param defaultValue default value.
     * @return property value as a <code>float</code>, 
     *         or <code>defaultValue</code>. 
     */
    public float getFloat(String name, float defaultValue) {
      String valueString = get(name);
      if (valueString == null)
        return defaultValue;
      try {
        return Float.parseFloat(valueString);
      } catch (NumberFormatException e) {
        return defaultValue;
      }
    }

    /**
     * Get the properties as a <code>Map<String,String></code>
     * 
     * @return Map<String,String> 
     */
    public Map<String,String> asMap() {
        return props;
    }
    
    @Override
    public Iterator<Entry<String, String>> iterator() {
        return props.entrySet().iterator();
    }
    
    public boolean isEmpty() {
        return props.isEmpty();
    }
}
