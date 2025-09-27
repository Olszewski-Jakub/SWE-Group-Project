package ie.universityofgalway.groupnine.infrastructure.auth.factory;

import ie.universityofgalway.groupnine.service.auth.factory.TokenFactory;
import ie.universityofgalway.groupnine.service.auth.port.RandomTokenPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenFactoryImpl extends TokenFactory {

    @Autowired
    public TokenFactoryImpl(RandomTokenPort randomTokenPort) {
        super(randomTokenPort);
    }
}
