package edu.upc.doiboard.doiboardbackend

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import javax.sql.DataSource

@SpringBootApplication
class DoiboardbackendApplication(val environment: Environment){
	@Value("\${server.address}") private val SERVER_ADDRESS: String = String()
	@Value("\${server.port}") private val SERVER_PORT: String = String()

	@Bean
	fun init() = CommandLineRunner {

		println("INIT APPLICATION APREN BACKEND - v.2.0")


		val serverAddress = environment.getProperty("SERVER_ADDRESS")
		val serverPort = environment.getProperty("SERVER_PORT")

		println("SERVER_ADDRESS: $serverAddress")
		println("SERVER_PORT: $serverPort")

		println("SERVER_ADDRESS: $SERVER_ADDRESS")
		println("SERVER_PORT: $SERVER_PORT")


	}

	@Bean
	fun checkDataSource(@Qualifier("dataSource") ds: DataSource): CommandLineRunner {
		return CommandLineRunner {
			if (ds is HikariDataSource) {
				println("__________________________________________________")
				println("✅ IDP DataSource URL: ${ds.jdbcUrl}")
				println("✅ Máximo de conexiones: ${ds.maximumPoolSize}")
				println("__________________________________________________")

			}
		}
	}


}

fun main(args: Array<String>) {
	runApplication<DoiboardbackendApplication>(*args)
}
