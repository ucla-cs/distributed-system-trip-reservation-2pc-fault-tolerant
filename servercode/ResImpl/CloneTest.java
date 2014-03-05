package ResImpl;

public class CloneTest
{
	public static void main(String[] args)
	{
		RMItem itemCar = new Car("A",110,5);
		RMItem itemFlight = new Flight(1,110,5);
		RMItem itemHotel = new Hotel("A",110,5);
		
		ReservableItem rCar = (ReservableItem) itemCar;
		ReservableItem rFlight = (ReservableItem) itemFlight;
		ReservableItem rHotel = (ReservableItem) itemHotel;
		
		System.out.println(rCar.clone() instanceof Car);
		System.out.println(rFlight.clone() instanceof Flight);
		System.out.println(rHotel.clone() instanceof Hotel);
		
		System.out.println(! (rCar.clone() instanceof Flight));
		System.out.println(! (rFlight.clone() instanceof Hotel));
		System.out.println(! (rHotel.clone() instanceof Car));
		
	}
}
