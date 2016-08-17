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
import mondrian.util.Pair;
import org.apache.log4j.Logger;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.userdetails.User;

import java.util.Iterator;
import java.util.List;

/**
 * Created by openbi on 11/7/2014.
 */
public class ParameterHandler implements IParameterHandler {

    Logger LOGGER = Logger.getLogger(KettleDynamicSchemaProcessor.class);

    public ParameterHandler() {}

    public void getParameters( Util.PropertyList connectInfo, IPentahoSession session, TransMeta transMeta )
    {
        try {
            Iterator<Pair<String, String>> itConnectInfo = connectInfo.iterator();
            while (itConnectInfo.hasNext()) {
                Pair<String, String> connectInfoPair = itConnectInfo.next();

                LOGGER.debug("Attempting to set Kettle parameter " + connectInfoPair.getKey() + " to "
                        + connectInfoPair.getValue());
                transMeta.setParameterValue(connectInfoPair.getKey(), connectInfoPair.getValue());
            }

            Iterator<String> itAttributeNames = session.getAttributeNames();
            while (itAttributeNames.hasNext()) {
                String attributeName = itAttributeNames.next();
                Object attributeValue = session.getAttribute(attributeName);

                LOGGER.debug("Attempting to set Kettle parameter " + attributeName + " to " + attributeValue.toString());
                transMeta.setParameterValue(attributeName, attributeValue.toString());

                if (attributeValue instanceof SecurityContextImpl) {
                    SecurityContextImpl sci = (SecurityContextImpl) attributeValue;
                    Authentication auth = sci.getAuthentication();
                    boolean isAuthenticated = auth.isAuthenticated();
                    LOGGER.debug("Attempting to set Kettle parameter Security.isAuthenticated to " + Boolean.toString(isAuthenticated));
                    transMeta.setParameterValue("Security.isAuthenticated", Boolean.toString(isAuthenticated));
                    GrantedAuthority[] authorities = auth.getAuthorities();
                    StringBuilder authorityList = new StringBuilder();
                    for (GrantedAuthority authority : authorities) {
                        if (authorityList.length() == 0) {
                            authorityList.append(authority.getAuthority());
                        } else {
                            authorityList.append(",").append(authority.getAuthority());
                        }

                        LOGGER.debug("Attempting to set Kettle parameter Security.role." + authority.getAuthority() + " to true");
                        transMeta.setParameterValue("Security.Role." + authority.getAuthority(), "true");
                    }
                    LOGGER.debug("Attempting to set Kettle parameter Security.Authorities to " + authorityList.toString());
                    transMeta.setParameterValue("Security.Authorities", authorityList.toString());
                    if (auth.getPrincipal() instanceof User) {
                        User user = (User) auth.getPrincipal();

                        LOGGER.debug("Attempting to set Kettle parameter Security.User.isAccountNonExpired to "
                                + Boolean.toString(user.isAccountNonExpired()));
                        transMeta.setParameterValue("Security.User.isAccountNonExpired",
                                Boolean.toString(user.isAccountNonExpired()));

                        LOGGER.debug("Attempting to set Kettle parameter Security.User.isAccountNonLocked to "
                                + Boolean.toString(user.isAccountNonLocked()));
                        transMeta.setParameterValue("Security.User.isAccountNonLocked",
                                Boolean.toString(user.isAccountNonLocked()));

                        LOGGER.debug("Attempting to set Kettle parameter Security.User.isCredentialsNonExpired to "
                                + Boolean.toString(user.isCredentialsNonExpired()));
                        transMeta.setParameterValue("Security.User.isCredentialsNonExpired",
                                Boolean.toString(user.isCredentialsNonExpired()));

                        LOGGER.debug("Attempting to set Kettle parameter Security.User.isEnabled to "
                                + Boolean.toString(user.isEnabled()));
                        transMeta.setParameterValue("Security.User.isEnabled", Boolean.toString(user.isEnabled()));
                    }

                } else if (attributeValue instanceof IUserSettingService) {
                    IUserSettingService userSettingService = (IUserSettingService) attributeValue;
                    List<IUserSetting> userSettings = userSettingService.getUserSettings();
                    Iterator<IUserSetting> itUserSettings = userSettings.iterator();
                    while (itUserSettings.hasNext()) {
                        IUserSetting setting = itUserSettings.next();
                        setting.getSettingName();
                        setting.getSettingValue();
                        LOGGER.debug("Attempting to set Kettle parameter UserSettting." + setting.getSettingName()
                                + " to " + setting.getSettingValue());
                        transMeta.setParameterValue("UserSetting." + setting.getSettingName(), setting.getSettingValue());
                    }
                }


            }

            LOGGER.debug("Attempting to set Kettle parameter SessionName to " + session.getName());
            transMeta.setParameterValue("SessionName", session.getName());

            LOGGER.debug("Attempting to set Kettle parameter SessionLocale to " + session.getLocale().toString());
            transMeta.setParameterValue("SessionLocale", session.getLocale().toString());

            LOGGER.debug("Attempting to set Kettle parameter SessionId to " + session.getId());
            transMeta.setParameterValue("SessionId", session.getId());
        } catch ( UnknownParamException ex )
        {
            //ignore
        }
    }
}
