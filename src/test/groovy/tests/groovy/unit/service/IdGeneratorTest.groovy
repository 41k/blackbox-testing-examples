package tests.groovy.unit.service

import root.service.IdGenerator
import spock.lang.Specification

class IdGeneratorTest extends Specification {

    def 'should generate unique id'() {
        given:
        def idGenerator = new IdGenerator()
        def setOfIds = new HashSet<String>()

        expect:
        0.upto(20, {
            def id = idGenerator.generate()
            assert id.size() == 8
            assert setOfIds.add(id)
        })
    }
}
