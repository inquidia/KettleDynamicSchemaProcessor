/**********************************************
* Kettle Dynamic Schema Processor
* Copyright (C) 2014 Inquidia Consulting
*
************************************************/

package org.inquidia.mondrian.dynamicschemaprocessor;

import mondrian.olap.Util;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Created by openbi on 11/7/2014.
 */
public interface IParameterHandler {

    public void getParameters( Util.PropertyList connectInfo, IPentahoSession session, TransMeta transMeta );

}
