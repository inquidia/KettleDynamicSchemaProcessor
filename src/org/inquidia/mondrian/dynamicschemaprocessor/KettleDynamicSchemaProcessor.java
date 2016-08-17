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

package org.inquidia.mondrian.dynamicschemaprocessor;

import mondrian.olap.Util;
import mondrian.spi.DynamicSchemaProcessor;
import org.apache.commons.lang.BooleanUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.inquidia.mondrian.utils.CacheUtils;
import org.inquidia.mondrian.utils.SchemaValueRowListener;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Implementation of DynamicSchemaProcessor which allows a Kettle transformation
 * to be run to return the schema.
 */
public class KettleDynamicSchemaProcessor implements DynamicSchemaProcessor {

    private static final Logger LOGGER = Logger.getLogger(KettleDynamicSchemaProcessor.class);

    public String processSchema( String schemaUrl, Util.PropertyList connectInfo ) throws Exception
    {
        if( LOGGER.getLevel() == null )
        {            LOGGER.setLevel( Level.ERROR );
        }

        boolean cacheEnabled = BooleanUtils.toBoolean( connectInfo.get( "cacheEnabled", "Y" ) );

        String schemaValue = null;

        IPentahoSession session = PentahoSessionHolder.getSession();
        String schemaCacheKey = CacheUtils.KeyBuilder.buildSessionSchemaKey(session, schemaUrl);

        if( cacheEnabled ) {

            schemaValue = (String) CacheUtils.get(CacheUtils.Region.SCHEMA_REGION, schemaCacheKey);
        }

        if( schemaValue == null ) {
            LOGGER.info("Starting schema processing " + schemaUrl);
            schemaValue = getSchema( schemaUrl, connectInfo, cacheEnabled );
            if( cacheEnabled ) {
                CacheUtils.put(CacheUtils.Region.SCHEMA_REGION, schemaCacheKey, schemaValue);
            }
        } else {
            LOGGER.info("Got schema from cache " + schemaUrl );
        }

        if( schemaValue == null )
        {
            LOGGER.error( schemaUrl + " Schema returned from transformation is null!");
            throw new KettleException( schemaUrl + " Schema returned from transformation is null!" );
        }

        LOGGER.debug( "Outputting schema\n" + schemaValue );
        return schemaValue;
    }

    private String getSchema( String schemaUrl, Util.PropertyList connectInfo, boolean cacheEnabled ) throws Exception
    {
        String transformationFile = connectInfo.get( "kettleTransFile" );
        LOGGER.debug( "Setting transformation file to " + transformationFile );

        String stepName = connectInfo.get( "kettleStepName", "OUTPUT" );
        LOGGER.debug( "Setting step name to " + stepName );
        String fieldName = connectInfo.get( "schemaFieldName", "schema" );
        LOGGER.debug( "Setting schema field name to " + fieldName );

        if( fieldName == null )
        {
            LOGGER.error( "Connection parameter schemaFieldName cannot be null." );
            throw new Exception( "Connection parameter schemaFieldName cannot be null." );
        }
        if( transformationFile == null )
        {
            LOGGER.error( "Connection parameter kettleTransFile cannot be null." );
            throw new Exception( "Connection parameter kettleTransFile cannot be null." );
        }
        if( stepName == null )
        {
            LOGGER.error( "Connection parameter kettleStepName cannot be null." );
            throw new Exception( "Connection parameter kettleStepName cannot be null." );
        }

        IPentahoSession session = PentahoSessionHolder.getSession();

        String normalizedTransformationFile = transformationFile;

        if( transformationFile.startsWith( "jcr-solution://" ) )
        {
            String filename = transformationFile.substring( 14 );
            filename = filename.replaceAll( "//", "/" );
            normalizedTransformationFile = "jcr-solution:http://127.0.0.1:8080/pentaho!" + filename;
        }

        TransMeta transMeta = null;
        String transCacheKey = CacheUtils.KeyBuilder.buildTransformationKey( normalizedTransformationFile );
        if( cacheEnabled )
        {
            transMeta = (TransMeta)CacheUtils.get( CacheUtils.Region.TRANSFORMATION_REGION, transCacheKey );
        }

        if( transMeta == null ) {
            InputStream inputStream = null;
            LOGGER.info("Opening transformation file " + normalizedTransformationFile);
            try {
                inputStream = KettleVFS.getInputStream(KettleVFS.getFileObject(normalizedTransformationFile));
            } catch (Exception ex) {
                LOGGER.error("Error reading file " + normalizedTransformationFile);
                LOGGER.error(ex.getMessage());
                throw ex;
            }

            if (inputStream == null) {
                LOGGER.error("Cannot find input file " + normalizedTransformationFile);
                throw new Exception("Cannot find input file " + normalizedTransformationFile);
            }

            transMeta = new TransMeta(inputStream, null, true, null, null);
            new TransMeta();
            inputStream.close();
            if( cacheEnabled ) {
                CacheUtils.put(CacheUtils.Region.TRANSFORMATION_REGION, transCacheKey, transMeta );
            }
        } else {
            LOGGER.info( "Opened transformation from cache. " + normalizedTransformationFile );
        }

        transMeta.clearParameters();

        String schemaText = Util.readVirtualFileAsString(schemaUrl);
        LOGGER.debug( "Attempting to set Kettle parameter InputSchemaText to " + schemaText );
        transMeta.setParameterValue( "InputSchemaText" , schemaText );

        if( connectInfo.get( "KettleParameterHandler" ) != null )
        {
            Class handlerClass = Class.forName( connectInfo.get( "KettleParameterHandler" ) );

            Class[] types = {  };
            Constructor constructor = handlerClass.getConstructor( types );

            Object[] parameters = {};
            IParameterHandler handler = (IParameterHandler)constructor.newInstance( parameters );

            LOGGER.info( "Using class " + connectInfo.get( "KettleParameterHandler" ) + " to handle parameters." );

            handler.getParameters( connectInfo, session, transMeta );

        } else {
            ParameterHandler handler = new ParameterHandler();
            LOGGER.info( "Using default parameter handler." );
            handler.getParameters(connectInfo, session, transMeta);

        }

        transMeta.setInternalKettleVariables();

        Trans trans = new Trans( transMeta );
        trans.setLog( new LogChannel( LOGGER ) );

        LOGGER.info( "Preparing transformation for execution." );
        trans.prepareExecution( transMeta.getArguments() );

        SchemaValueRowListener listener = null;
        final List<StepMetaDataCombi> stepList = trans.getSteps();

        LOGGER.info( "Searching for step " + stepName );
        for( StepMetaDataCombi aStepList : stepList )
        {
            if( aStepList.stepname.equalsIgnoreCase( stepName ) )
            {
                final RowMetaInterface row = transMeta.getStepFields( aStepList.stepname );
                listener = new SchemaValueRowListener( fieldName );
                aStepList.step.addRowListener( listener );

                break;
            }
        }

        if( listener == null ) {
            LOGGER.error( "Cannot find the specified transformation step " + stepName );
            throw new KettleStepException( "Cannot find the specified transformation step " + stepName );
        }

        LOGGER.info( "Starting transformation." );
        trans.startThreads();
        trans.waitUntilFinished();
        trans.cleanup();
        LOGGER.info( "Transformation finished" );

        if( trans.getErrors() > 0 )
        {
            LOGGER.error( "Transformation finished with errors." );
            throw new KettleException( "Transformation finished with errors" );
        }


        return listener.getSchemaValue();
    }


}
