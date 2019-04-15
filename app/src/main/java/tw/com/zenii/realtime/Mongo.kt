package tw.com.zenii.realtime

import org.ksoap2.SoapEnvelope
import org.ksoap2.serialization.SoapObject
import org.ksoap2.serialization.SoapPrimitive
import org.ksoap2.serialization.SoapSerializationEnvelope
import org.ksoap2.transport.HttpTransportSE
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class Mongo {

     fun call(method: String, arg0: String): String? {
        val NAMESPACE = "http://ws.zenii.com.tw/"
        // 239 92
        val URL = "http://192.168.1.92:9080/MongoService/services/Query?wsdl"

        val request = SoapObject(NAMESPACE, method)
        request.addProperty("arg0", arg0)

        val envelope = SoapSerializationEnvelope(SoapEnvelope.VER11)
        envelope.setOutputSoapObject(request)

        val httpTransport = HttpTransportSE(URL)
        httpTransport.debug = true

        // If the response type is a primitive type like Integer or Boolean, use SoapPrimitive;
        // otherwise, use SoapObject for response.
        var response: SoapPrimitive? = null
        try {
            httpTransport.call(null, envelope)
            response = envelope.response as SoapPrimitive
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: TypeCastException) {
            e.printStackTrace()
        }

        return response?.toString()
    }
}