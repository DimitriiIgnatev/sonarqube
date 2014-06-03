/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualityprofile;

import com.google.common.collect.Multimap;
import org.sonar.api.ServerComponent;
import org.sonar.api.rule.RuleKey;
import org.sonar.core.permission.GlobalPermissions;
import org.sonar.core.qualityprofile.db.ActiveRuleKey;
import org.sonar.core.qualityprofile.db.QualityProfileKey;
import org.sonar.server.qualityprofile.index.ActiveRuleIndex;
import org.sonar.server.rule.index.RuleQuery;
import org.sonar.server.search.IndexClient;
import org.sonar.server.user.UserSession;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public class QProfileService implements ServerComponent {

  private final IndexClient index;
  private final RuleActivator ruleActivator;
  private final QProfileBackuper backuper;

  public QProfileService(IndexClient index, RuleActivator ruleActivator, QProfileBackuper backuper) {
    this.index = index;
    this.ruleActivator = ruleActivator;
    this.backuper = backuper;
  }

  @CheckForNull
  public ActiveRule getActiveRule(ActiveRuleKey key) {
    return index.get(ActiveRuleIndex.class).getByKey(key);
  }

  public List<ActiveRule> findActiveRulesByRule(RuleKey key) {
    return index.get(ActiveRuleIndex.class).findByRule(key);
  }

  public List<ActiveRule> findActiveRulesByProfile(QualityProfileKey key) {
    return index.get(ActiveRuleIndex.class).findByProfile(key);
  }

  /**
   * Activate a rule on a Quality profile. Update configuration (severity/parameters) if the rule is already
   * activated.
   */
  public List<ActiveRuleChange> activate(RuleActivation activation) {
    verifyAdminPermission();
    return ruleActivator.activate(activation);
  }

  /**
   * Deactivate a rule on a Quality profile. Does nothing if the rule is not activated, but
   * fails (fast) if the rule or the profile does not exist.
   */
  public List<ActiveRuleChange> deactivate(ActiveRuleKey key) {
    verifyAdminPermission();
    return ruleActivator.deactivate(key);
  }


  public Multimap<String, String> bulkActivate(RuleQuery ruleQuery, QualityProfileKey profile) {
    verifyAdminPermission();
    return ruleActivator.bulkActivate(ruleQuery, profile);
  }

  public Multimap<String, String> bulkDeactivate(RuleQuery ruleQuery, QualityProfileKey profile) {
    verifyAdminPermission();
    return ruleActivator.bulkDeactivate(ruleQuery, profile);
  }

  public void backup(QualityProfileKey key, Writer writer) {
    // Allowed to non-admin users (see http://jira.codehaus.org/browse/SONAR-2039)
    backuper.backup(key, writer);
  }

  /**
   * @deprecated used only by Ruby on Rails. Use {@link #backup(org.sonar.core.qualityprofile.db.QualityProfileKey, java.io.Writer)}
   */
  @Deprecated
  public String backup(QualityProfileKey key) {
    StringWriter output = new StringWriter();
    backup(key, output);
    return output.toString();
  }

  public void restore(Reader backup) {
    verifyAdminPermission();
    backuper.restore(backup);
  }

  /**
   * @deprecated used only by Ruby on Rails. Use {@link #restore(java.io.Reader)}
   */
  @Deprecated
  public void restore(String backup) {
    restore(new StringReader(backup));
  }

  public void resetForLang(String lang) {
    // TODO
    verifyAdminPermission();
  }

  public void copy(QualityProfileKey key, String newName) {
    // TODO
    verifyAdminPermission();
  }

  public void delete(QualityProfileKey key) {
    // TODO
    verifyAdminPermission();
  }

  public void rename(QualityProfileKey key, String newName) {
    // TODO
    verifyAdminPermission();
  }

  //public void create(NewQualityProfile newProfile) {
  // TODO
  //verifyAdminPermission();
  //}

  public void setParent(QualityProfileKey key, @Nullable QualityProfileKey parent) {
    // TODO
    verifyAdminPermission();
  }

  /**
   * Set the given quality profile as default for the related language
   */
  public void setDefault(QualityProfileKey key) {
    // TODO
    verifyAdminPermission();
  }

  private void verifyAdminPermission() {
    UserSession.get().checkLoggedIn();
    UserSession.get().checkGlobalPermission(GlobalPermissions.QUALITY_PROFILE_ADMIN);
  }
}
