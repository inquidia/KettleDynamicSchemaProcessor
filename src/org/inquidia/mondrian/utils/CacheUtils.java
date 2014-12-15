/**********************************************
* Kettle Dynamic Schema Processor
* Copyright (C) 2014 Inquidia Consulting
*
************************************************/

package org.inquidia.mondrian.utils;

import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by openbi on 11/7/2014.
 */
public class CacheUtils {

    public enum Region {
        SCHEMA_REGION( "InquidiaKettleDspSchemaRegion" ), TRANSFORMATION_REGION( "InquidiaKettleDspTransRegion" );

        private String name;

        private Region( String region )
        {
            this.name = region;
        }

        private String getName() {
            return name;
        }
    }

    public static class KeyBuilder {

        private KeyBuilder() {};

        public static String buildSessionSchemaKey( IPentahoSession session, String schemaUrl )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( session.getName() ).append( "/SCHEMA_" );
            builder.append( schemaUrl );
            return builder.toString();
        }

        public static String buildTransformationKey( String transformationName )
        {
            return transformationName;
        }
    }

    private CacheUtils() {}

    public static void put( Region region, String key, Object value )
    {
        ICacheManager manager = CacheUtils.getCacheManager( region );

        if( manager != null ) {
            manager.putInRegionCache( region.getName(), key, value );
        }
    }

    public static Object get( Region region, String key ) {
        Object value = null;
        ICacheManager manager = CacheUtils.getCacheManager( region );

        if( manager != null ) {
            value = manager.getFromRegionCache( region.getName(), key );
        }

        return value;
    }

    public static Object get(IPentahoSession session, String key) {

        Object value = null;
        ICacheManager manager = PentahoSystem.getCacheManager(session);

        if (manager != null) {
            value = manager.getFromSessionCache(session, key);
        }

        return value;
    }

    public static void remove(Region region, String key) {

        ICacheManager manager = CacheUtils.getCacheManager(region);

        if (manager != null) {
            manager.removeFromRegionCache(region.getName(), key);
        }
    }

    public static void remove(Region region) {

        ICacheManager manager = CacheUtils.getCacheManager(region);

        if (manager != null) {
            manager.removeRegionCache(region.getName());
        }
    }

    public static void remove(IPentahoSession session, String key) {

        ICacheManager manager = PentahoSystem.getCacheManager(session);

        if (manager != null) {
            manager.removeFromSessionCache(session, key);
        }
    }

    public static void clear( Region region )
    {
        ICacheManager cacheManager = getCacheManager( region );
        cacheManager.clearCache();
    }

    public static void clearAllRegions() {
        ICacheManager cacheManager = getCacheManager( Region.SCHEMA_REGION );
        cacheManager.clearCache();
        cacheManager = getCacheManager( Region.TRANSFORMATION_REGION );
        cacheManager.clearCache();
    }

    private static ICacheManager getCacheManager(Region region) {

        ICacheManager cacheManager = PentahoSystem.getCacheManager(null);

        if ((cacheManager != null) && (!cacheManager.cacheEnabled(region.getName()))) {
            cacheManager.addCacheRegion(region.getName());
        }

        return cacheManager;
    }
}
