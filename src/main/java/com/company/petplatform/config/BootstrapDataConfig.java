package com.company.petplatform.config;

import com.company.petplatform.auth.RoleEntity;
import com.company.petplatform.auth.RoleRepository;
import com.company.petplatform.auth.UserEntity;
import com.company.petplatform.auth.UserRepository;
import com.company.petplatform.auth.UserRoleEntity;
import com.company.petplatform.auth.UserRoleRepository;
import com.company.petplatform.notification.NotificationEntity;
import com.company.petplatform.notification.NotificationRepository;
import com.company.petplatform.notification.NotificationSubscriptionEntity;
import com.company.petplatform.notification.NotificationSubscriptionRepository;
import com.company.petplatform.reporting.IndicatorDefinitionEntity;
import com.company.petplatform.reporting.IndicatorDefinitionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapDataConfig {

  @Bean
  CommandLineRunner bootstrapUsers(
      UserRepository userRepository,
      RoleRepository roleRepository,
      UserRoleRepository userRoleRepository,
      PasswordEncoder passwordEncoder,
      IndicatorDefinitionRepository indicatorDefinitionRepository,
      NotificationSubscriptionRepository notificationSubscriptionRepository,
      NotificationRepository notificationRepository) {
    return args -> {
      if (userRepository.findByUsername("admin").isPresent()) {
        return;
      }
      UserEntity user = new UserEntity();
      user.setUsername("admin");
      user.setDisplayName("System Admin");
      user.setPasswordHash(passwordEncoder.encode("admin1234"));
      user.setFailedLoginCount(0);
      user.setStatus("ACTIVE");
      UserEntity savedUser = userRepository.save(user);

      RoleEntity adminRole = roleRepository.findByCode("ADMIN").orElse(null);
      if (adminRole != null) {
        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setUserId(savedUser.getId());
        userRole.setRoleId(adminRole.getId());
        userRoleRepository.save(userRole);
      }

      if (indicatorDefinitionRepository.findByIndicatorCode("INV_LOW_STOCK_CNT").isEmpty()) {
        IndicatorDefinitionEntity indicator = new IndicatorDefinitionEntity();
        indicator.setIndicatorCode("INV_LOW_STOCK_CNT");
        indicator.setIndicatorName("Low stock sku count");
        indicator.setDomain("INVENTORY");
        indicator.setMetricType("COUNT");
        indicator.setExpressionSql("SELECT COUNT(*) FROM inventory WHERE available_qty <= alert_threshold");
        indicator.setStatus("ACTIVE");
        indicator.setCreatedBy(savedUser.getId());
        indicatorDefinitionRepository.save(indicator);
      }

      NotificationSubscriptionEntity sub = new NotificationSubscriptionEntity();
      sub.setUserId(savedUser.getId());
      sub.setCategory("INVENTORY_ALERT");
      sub.setEnabledFlag(true);
      notificationSubscriptionRepository.save(sub);

      NotificationEntity welcome = new NotificationEntity();
      welcome.setNotificationNo("N-" + System.currentTimeMillis());
      welcome.setUserId(savedUser.getId());
      welcome.setChannel("INTERNAL");
      welcome.setCategory("SYSTEM");
      welcome.setTitle("Welcome");
      welcome.setContentText("System initialized");
      welcome.setStatus("UNREAD");
      notificationRepository.save(welcome);
    };
  }
}
