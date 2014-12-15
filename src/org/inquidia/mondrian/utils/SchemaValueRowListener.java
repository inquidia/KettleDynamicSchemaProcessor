/**********************************************
* Kettle Dynamic Schema Processor
* Copyright (C) 2014 Inquidia Consulting
*
************************************************/

package org.inquidia.mondrian.utils;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;

/**
 * Created by openbi on 11/5/2014.
 */
public class SchemaValueRowListener implements RowListener{

    private String schemaFieldName;
    private String schemaValue;

    public SchemaValueRowListener( String schemaFieldName )
    {
        this.schemaFieldName = schemaFieldName;
    }

    public void rowReadEvent( RowMetaInterface rowMeta, Object[] row )
    {}

    public void errorRowWrittenEvent( RowMetaInterface rowMetaInterface, Object[] row )
    {}

    public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException
    {
        try {
            schemaValue = rowMeta.getString(row, schemaFieldName, null);
        } catch ( KettleValueException ex )
        {
            throw new KettleStepException( ex.getMessage(), ex );
        }
    }

    public String getSchemaFieldName() {
        return schemaFieldName;
    }

    public void setSchemaFieldName(String schemaFieldName) {
        this.schemaFieldName = schemaFieldName;
    }

    public String getSchemaValue() {
        return schemaValue;
    }

    public void setSchemaValue(String schemaValue) {
        this.schemaValue = schemaValue;
    }
}
