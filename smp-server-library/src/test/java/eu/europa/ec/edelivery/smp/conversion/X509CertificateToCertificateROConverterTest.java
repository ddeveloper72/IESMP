/*-
 * #START_LICENSE#
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.InputStream;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class X509CertificateToCertificateROConverterTest {
    static {
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    private static Object[] testCases() {
        return new Object[][]{
                // filename, subject, issuer, serial number, clientCertHeader, certificateId, certKeyType
                {
                        "cert-escaped-chars.pem",
                        "CN=Escape characters \\,\\\\\\#\\+\\<\\>\\\"\\=,OU=CEF,O=DIGIT,C=BE",
                        "CN=Escape characters \\,\\\\\\#\\+\\<\\>\\\"\\=,OU=CEF,O=DIGIT,C=BE",
                        "5c1bb275",
                        "sno=5c1bb275&subject=CN%3DEscape+characters+%5C%2C%5C%5C%5C%23%5C%2B%5C%3C%5C%3E%5C%22%5C%3D%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE&validfrom=Dec+20+15%3A17%3A09+2018+GMT&validto=Dec+17+15%3A17%3A09+2028+GMT&issuer=CN%3DEscape+characters+%5C%2C%5C%5C%5C%23%5C%2B%5C%3C%5C%3E%5C%22%5C%3D%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE",
                        "CN=Escape characters \\,\\\\\\#\\+\\<\\>\\\"\\=,O=DIGIT,C=BE:000000005c1bb275",
                        "RSA"
                },
                {
                        "cert-nonAscii.pem",
                        "CN=NonAscii chars:  àøýßĉæãäħ,OU=CEF,O=DIGIT,C=BE",
                        "CN=NonAscii chars:  àøýßĉæãäħ,OU=CEF,O=DIGIT,C=BE",
                        "5c1bb38d",
                        "sno=5c1bb38d&subject=CN%3DNonAscii+chars%3A++%C3%A0%C3%B8%C3%BD%C3%9F%C4%89%C3%A6%C3%A3%C3%A4%C4%A7%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE&validfrom=Dec+20+15%3A21%3A49+2018+GMT&validto=Dec+17+15%3A21%3A49+2028+GMT&issuer=CN%3DNonAscii+chars%3A++%C3%A0%C3%B8%C3%BD%C3%9F%C4%89%C3%A6%C3%A3%C3%A4%C4%A7%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE",
                        "CN=NonAscii chars:  aøyßcæaaħ,O=DIGIT,C=BE:000000005c1bb38d",
                        "RSA"
                },
                {
                        "cert-with-email.pem",
                        "CN=Cert with email,OU=CEF,O=DIGIT,C=BE",
                        "CN=Cert with email,OU=CEF,O=DIGIT,C=BE",
                        "5c1bb358",
                        "sno=5c1bb358&subject=CN%3DCert+with+email%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE&validfrom=Dec+20+15%3A20%3A56+2018+GMT&validto=Dec+17+15%3A20%3A56+2028+GMT&issuer=CN%3DCert+with+email%2COU%3DCEF%2CO%3DDIGIT%2CC%3DBE",
                        "CN=Cert with email,O=DIGIT,C=BE:000000005c1bb358",
                        "RSA"
                },
                {
                        "cert-smime.pem",
                        "C=BE,O=European Commission,OU=PEPPOL TEST SMP,CN=edelivery_sml",
                        "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA - G2,OU=FOR TEST ONLY,O=OpenPEPPOL AISBL,C=BE",
                        "3cfe6b37e4702512c01e71f9b9175464",
                        "sno=3cfe6b37e4702512c01e71f9b9175464&subject=C%3DBE%2CO%3DEuropean+Commission%2COU%3DPEPPOL+TEST+SMP%2CCN%3Dedelivery_sml&validfrom=Sep+21+00%3A00%3A00+2018+GMT&validto=Sep+10+23%3A59%3A59+2020+GMT&issuer=CN%3DPEPPOL+SERVICE+METADATA+PUBLISHER+TEST+CA+-+G2%2COU%3DFOR+TEST+ONLY%2CO%3DOpenPEPPOL+AISBL%2CC%3DBE",
                        "CN=edelivery_sml,O=European Commission,C=BE:3cfe6b37e4702512c01e71f9b9175464",
                        "RSA"
                },
                {
                        "test-mvRdn.crt",
                        "C=BE,O=DIGIT,2.5.4.5=#130131+2.5.4.42=#0c046a6f686e+CN=SMP_receiverCN",
                        "C=BE,O=DIGIT,2.5.4.5=#130131+2.5.4.42=#0c046a6f686e+CN=SMP_receiverCN",
                        "123456789101112",
                        "sno=123456789101112&subject=C%3DBE%2CO%3DDIGIT%2C2.5.4.5%3D%23130131%2B2.5.4.42%3D%230c046a6f686e%2BCN%3DSMP_receiverCN&validfrom=Dec+09+13%3A14%3A11+2019+GMT&validto=Feb+01+13%3A14%3A11+2021+GMT&issuer=C%3DBE%2CO%3DDIGIT%2C2.5.4.5%3D%23130131%2B2.5.4.42%3D%230c046a6f686e%2BCN%3DSMP_receiverCN",
                        "CN=SMP_receiverCN,O=DIGIT,C=BE:0123456789101112",
                        "RSA"
                },
                {
                        "long-serial-number.crt",
                        "C=EU,O=Ministerio de large Serial Number,CN=ncp-ppt.test.ehealth",
                        "C=EU,O=Ministerio de large Serial Number,CN=ncp-ppt.test.ehealth",
                        "a33e30cd250b17267b13bec",
                        "sno=a33e30cd250b17267b13bec&subject=C%3DEU%2CO%3DMinisterio+de+large+Serial+Number%2CCN%3Dncp-ppt.test.ehealth&validfrom=May+26+08%3A50%3A08+2022+GMT&validto=May+27+08%3A50%3A08+2027+GMT&issuer=C%3DEU%2CO%3DMinisterio+de+large+Serial+Number%2CCN%3Dncp-ppt.test.ehealth",
                        "CN=ncp-ppt.test.ehealth,O=Ministerio de large Serial Number,C=EU:0a33e30cd250b17267b13bec", // note the leading 0
                        "RSA"
                },
                {
                        "ecdsa_nist_p256v1.cer",
                        "C=EU,O=DIGIT,CN=ECDSA_NIST_P256V1OU",
                        "C=EU,O=DIGIT,CN=ECDSA_NIST_P256V1OU",
                        "2710",
                        "sno=2710&subject=C%3DEU%2CO%3DDIGIT%2CCN%3DECDSA_NIST_P256V1OU&validfrom=Nov+10+06%3A40%3A56+2022+GMT&validto=Nov+08+06%3A40%3A56+2032+GMT&issuer=C%3DEU%2CO%3DDIGIT%2CCN%3DECDSA_NIST_P256V1OU",
                        "CN=ECDSA_NIST_P256V1OU,O=DIGIT,C=EU:0000000000002710",
                        "EC"
                },
                {
                        "ed25519.cert",
                        "C=EU,O=DIGIT,OU=EDELIVERY,CN=Test-Ed25519",
                        "C=EU,O=DIGIT,OU=EDELIVERY,CN=Test-Ed25519",
                        "2710",
                        "sno=2710&subject=C%3DEU%2CO%3DDIGIT%2COU%3DEDELIVERY%2CCN%3DTest-Ed25519&validfrom=Nov+14+13%3A14%3A05+2022+GMT&validto=Nov+12+13%3A14%3A05+2032+GMT&issuer=C%3DEU%2CO%3DDIGIT%2COU%3DEDELIVERY%2CCN%3DTest-Ed25519",
                        "CN=Test-Ed25519,O=DIGIT,C=EU:0000000000002710",
                        "Ed25519"
                },
                {
                        "ed448.cert",
                        "CN=Test-Ed448,OU=EDELIVERY,O=DIGIT,C=EU",
                        "CN=Test-Ed448,OU=EDELIVERY,O=DIGIT,C=EU",
                        "6430e8fc",
                        "sno=6430e8fc&subject=CN%3DTest-Ed448%2COU%3DEDELIVERY%2CO%3DDIGIT%2CC%3DEU&validfrom=Apr+08+04%3A09%3A32+2023+GMT&validto=Apr+08+04%3A09%3A32+2033+GMT&issuer=CN%3DTest-Ed448%2COU%3DEDELIVERY%2CO%3DDIGIT%2CC%3DEU",
                        "CN=Test-Ed448,O=DIGIT,C=EU:000000006430e8fc",
                        "Ed448"
                },

        };
    }


    X509CertificateToCertificateROConverter testInstance = new X509CertificateToCertificateROConverter();

    @ParameterizedTest
    @MethodSource("testCases")
    void testConvert(String filename,
                            String subject,
                            String issuer,
                            String serialNumber,
                            String clientCertHeader,
                            String certificateId,
                            String publicKeyType) throws CertificateException {


        // given
        X509Certificate certificate = getCertificate(filename);

        // when
        CertificateRO certRo = testInstance.convert(certificate);

        //then
        assertEquals(subject, certRo.getSubject());
        assertEquals(issuer, certRo.getIssuer());
        assertEquals(serialNumber, certRo.getSerialNumber());
        assertEquals(clientCertHeader, certRo.getClientCertHeader());
        assertEquals(certificateId, certRo.getCertificateId());
        assertNotNull(certRo.getEncodedValue());
        assertEquals(certificate.getNotBefore().toInstant().atOffset(ZoneOffset.UTC), certRo.getValidFrom());
        assertEquals(certificate.getNotAfter().toInstant().atOffset(ZoneOffset.UTC), certRo.getValidTo());
        assertEquals(publicKeyType, certRo.getPublicKeyType());
    }

    X509Certificate getCertificate(String filename) throws CertificateException {
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        InputStream is = X509CertificateToCertificateROConverterTest.class.getResourceAsStream("/certificates/" + filename);
        return (X509Certificate) fact.generateCertificate(is);

    }
}
