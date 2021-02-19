package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {

		Hello hello = new Hello();
		hello.setData("hello");

		String data = hello.getData();
		System.out.println("data : " + data);

		SpringApplication.run(JpashopApplication.class, args);
	}

	/**
	 * Hibernate5Module 등록
	 *
	 * @return
	 */
	@Bean
	Hibernate5Module hibernate5Module() {

		Hibernate5Module hibernate5Module = new Hibernate5Module();

		// 다음과 같이 설정하면 강제로 지연 로딩 가능
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);

		return hibernate5Module;
	}

}
