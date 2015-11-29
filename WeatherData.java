import java.io.Serializable;
import java.util.*;
import scala.collection.mutable.ArrayBuffer;

public class WeatherData implements Serializable
{
	public String lang;
	public String location;
	public String condition;
	public String timezone;
	public Long count;
	public String text;
	public String screenname;
	public String country;
	public String source;
	public ArrayBuffer coordinates;
}

