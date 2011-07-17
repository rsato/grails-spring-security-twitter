import com.the6hours.grails.springsecurity.twitter.TwitterAuthProvider
import com.the6hours.grails.springsecurity.twitter.TwitterAuthFilter
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import com.the6hours.grails.springsecurity.twitter.DefaultConnectedTwitterAuthDao
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler

/* Copyright 2006-2010 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
class SpringSecurityTwitterGrailsPlugin {

    // the plugin version
    def version = "0.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.3 > *"

    Map dependsOn = ['springSecurityCore': '1.1.2 > *']

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    String author = 'Igor Artamonov'
    String authorEmail = 'igor@artamonov.ru'
    String title = 'Twitter authentication for Spring Security plugin'
    String description = 'Twitter authentication support for the Spring Security plugin.'

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/spring-security-twitter"

    def doWithSpring = {
        //def SpringSecurityUtils = classLoader.loadClass('org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils')

        def conf = SpringSecurityUtils.securityConfig
        if (!conf) {
            return
        }

        println 'Configuring Spring Security Twitter ...'

        SpringSecurityUtils.loadSecondaryConfig 'DefaultTwitterSecurityConfig'

        // have to get again after overlaying DefaultTwitterSecurityConfig
        conf = SpringSecurityUtils.securityConfig

        if (!conf.twitter.bean.dao) {
            twitterConnectedAuthDao(DefaultConnectedTwitterAuthDao) {
                domainClassName = conf.twitter.domain.classname
                connectionPropertyName = conf.twitter.domain.connectionPropertyName
                userDomainClassName = conf.userLookup.userDomainClassName
                rolesPropertyName = conf.userLookup.authoritiesPropertyName

                grailsApplication = ref('grailsApplication')
            }
            conf.twitter.bean.dao = 'twitterConnectedAuthDao'
        }

        if (conf.twitter.bean.provider) {
            SpringSecurityUtils.registerProvider conf.twitter.bean.provider.toString()
        } else {
            SpringSecurityUtils.registerProvider 'twitterAuthProvider'
            conf.twitter.bean.provider = 'twitterAuthProvider'
            twitterAuthProvider(TwitterAuthProvider) {
                authDao = ref(conf.twitter.bean.dao)
                if (conf.twitter.bean.listener) {
                    listener = ref(conf.twitter.bean.listener)
                }
            }
        }

        if (conf.twitter.bean.filter) {
            SpringSecurityUtils.registerFilter conf.twitter.bean.filter.toString(), SecurityFilterPosition.OPENID_FILTER
        } else {
            SpringSecurityUtils.registerFilter 'twitterAuthFilter', SecurityFilterPosition.OPENID_FILTER
            conf.twitter.bean.filter = 'twitterAuthFilter'
            twitterAuthFilter(TwitterAuthFilter, conf.twitter.filter.processUrl) {
                rememberMeServices = ref('rememberMeServices')
                authenticationManager = ref('authenticationManager')
                authenticationSuccessHandler = ref('authenticationSuccessHandler')
                authenticationFailureHandler = ref('authenticationFailureHandler')
                authenticationDetailsSource = ref('authenticationDetailsSource')
                sessionAuthenticationStrategy = ref('sessionAuthenticationStrategy')
                filterProcessesUrl =  conf.twitter.filter.processUrl
                consumerKey = conf.twitter.app.consumerKey
                consumerSecret = conf.twitter.app.consumerSecret
                if (conf.twitter.popup) {
                    authenticationSuccessHandler = new SimpleUrlAuthenticationSuccessHandler(conf.twitter.filter.processPopupUrl)
                }
            }
        }

    }

    def doWithApplicationContext = { applicationContext ->
    }

}