/******************************************************************************
 * Kettle Dynamic Schema Processor
 * Copyright (C) 2014-2016 Inquidia Consulting
 *
 ******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

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
