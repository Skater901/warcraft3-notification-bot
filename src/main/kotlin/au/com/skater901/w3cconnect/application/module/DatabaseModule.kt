package au.com.skater901.w3cconnect.application.module

import au.com.skater901.w3cconnect.core.dao.ChannelNotificationDAO
import au.com.skater901.w3cconnect.core.dao.jdbi.JdbiChannelNotificationDAO
import com.google.inject.AbstractModule

class DatabaseModule : AbstractModule() {
    override fun configure() {
        bind(ChannelNotificationDAO::class.java).to(JdbiChannelNotificationDAO::class.java)
    }
}