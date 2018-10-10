package ipaneltv.uuids;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class UUIDHoldings {

	private static HashMap<String, UUID> ids = new HashMap<String, UUID>();
	private static HashSet<String> idset = new HashSet<String>();

	static final void register(String name, String id) {
		synchronized (ids) {
			if (idset.contains(id))
				throw new RuntimeException("uuids is in use!(" + id + "),use other one!");
			ids.put(name, UUID.fromString(id));
			idset.add(id);
		}
	}

	public static String get(String name) {
		synchronized (ids) {
			return ids.get(name).toString();
		}
	}

	static {
		UUIDHoldings.register("ncwasu", NcWasuUUIDs.ID);
		UUIDHoldings.register("ncwasu_search", NcWasuUUIDs.ID_SEARCH);
		UUIDHoldings.register("hunan", HunanUUIDs.ID);
		UUIDHoldings.register("hunan_search", HunanUUIDs.ID_SEARCH);
		UUIDHoldings.register("shaanxi", ShaanxiUUIDs.ID);
		UUIDHoldings.register("shaanxi_search", ShaanxiUUIDs.ID_SEARCH);
		UUIDHoldings.register("shanxi", ShanxiUUIDs.ID);
		UUIDHoldings.register("shanxi_search", ShanxiUUIDs.ID_SEARCH);
		UUIDHoldings.register("chongqing", ChongqingUUIDs.ID);
		UUIDHoldings.register("chongqing_search", ChongqingUUIDs.ID_SEARCH);
		UUIDHoldings.register("dalian", DalianUUIDs.ID);
		UUIDHoldings.register("dalian_search", DalianUUIDs.ID_SEARCH);
		UUIDHoldings.register("ipanel", iPanelUUIDs.ID);
		UUIDHoldings.register("ipanel_search", iPanelUUIDs.ID_SEARCH);
		UUIDHoldings.register("jiangxi", JiangxiUUIDs.ID);
		UUIDHoldings.register("jiangxi_search", JiangxiUUIDs.ID_SEARCH);
		UUIDHoldings.register("homed", HomedUUIDs.ID);
		UUIDHoldings.register("homed_search", HomedUUIDs.ID_SEARCH);
		UUIDHoldings.register("henan", HenanUUIDs.ID);
		UUIDHoldings.register("henan_search", HenanUUIDs.ID_SEARCH);
		UUIDHoldings.register("guangdong", GuangdongUUIDs.ID);
		UUIDHoldings.register("guangdong_search", GuangdongUUIDs.ID_SEARCH);
		UUIDHoldings.register("topway", TopwayUUIDs.ID);
		UUIDHoldings.register("topway_search", TopwayUUIDs.ID_SEARCH);
		UUIDHoldings.register("yunnan", YunNanUUIDs.ID);
		UUIDHoldings.register("yunnan_search", YunNanUUIDs.ID_SEARCH);
		UUIDHoldings.register("zibo", ZiboUUIDs.ID);
		UUIDHoldings.register("zibo_search", ZiboUUIDs.ID_SEARCH);
	}
}
/*- 以下备选，选取以后请删除相应条目
 749099db-8936-481a-8456-4615d5ebdbc7
 ac9e1dae-d8df-42f0-8168-8a4282448ed5
 70817d9b-9998-4f64-bc62-553eaf5a1e80
 42af69ed-9994-44a9-a9dc-286c3dd91423
 618461a0-7d56-4a6f-a6dd-d02ee29a224c
 68b2f32b-7c98-4226-a66e-d42cee8a9aa1
 fe4eca98-19c6-46ce-a089-57c85ca26303
 9c7b1c26-493c-4b44-a7a6-e25ff23e5518
 6902dcbd-e546-4df1-ae45-fb09480f9a4b
 129341df-03c9-443d-9773-7413defb4288
 4eb7c706-21aa-4319-9571-981a8061163a
 2ece16bf-9476-4a4e-a8a6-335488f14324
 d975c934-8c9e-42bf-ae97-e5c83a866774
 2b197a16-f8ac-41d1-878d-5342fb4e66ad
 a586f850-cec6-4910-8646-3bb0ae70f527
 1a475e22-a88e-4e8b-98b9-7d677ba4d6b8
 203223b7-43a5-4197-9d26-44a7a3b0b921
 1e38b671-ab35-4b3a-98cf-b3356ce06ac7
 1065e1eb-5a6b-43e6-bac8-3395f796a2e2
 99552873-8d20-4f9f-8fb4-18adeafe1459
 2fc2b77e-0a37-4940-a4c7-9d6822243191
 ce1d40cb-c9a3-40eb-80fc-7c0268c35ac2
 c0235c9d-d82a-4ce3-bf74-df9a069ba962
 e32e649e-3860-4fff-968d-d274ca0fcbab
 8b694a47-54bd-47de-bb04-96b48c45e3b5
 9a3ad835-8c78-4e4e-bf2a-7e0d69c519eb
 6815b851-915d-440a-b6ea-fd8eeb2596b9
 4b2e370e-16b1-45f4-9f0d-12a198fcb06d
 50721de8-468a-4b1a-84f5-31132b88cd8b
 cdfdfe26-bf9f-4353-a1bb-de11891113f2
 bb78672e-895f-446a-9a7e-c6e196ba91c5
 25f61203-73a2-4016-8dd9-a6a67b85a0f4
 643178f1-2231-4e67-b30b-6d564ad160cf
 bdb6efe9-471e-4fe5-ab67-42f370b1bba7
 02cb6e47-3589-4ab1-aae4-03ed10777168
 397950da-a995-46b4-9231-92b775366640
 bb95e5b8-7f49-47e2-a37b-ec423098f026
 3f8016f3-3816-4562-a0d1-fde1a5b0c189
 9f6cb6c3-6da2-45b8-892a-845ef7908a41
 e6b4bbdb-6118-4e03-80df-986dc5ae9593
 89c66629-3aa3-453c-9640-d50fd7abfb72
 1f5bc239-7a96-476f-8516-510c6b323dcc
 75e8cc40-779f-4850-a0ef-6333d19e5c8b
 0bed93cd-241b-4264-9e20-dfc78a035577
 79f9e767-aa78-4340-a88c-eecc96953e12
 44b1d0c1-3b53-4b85-940e-9284798277be
 b4c9ae16-eba0-4f1b-a441-4826eb1b01fd
 a727715d-1b21-4613-b3cb-590e6e4f658c
 3abc7c67-8206-4a73-a855-709de2cbc700
 92c4088d-2568-4ec4-95bb-9baeaee3ed58
 a863d35f-ebb7-4c2c-99be-ff60b4ff5d7f
 adf58884-36a1-4041-8577-deef49820d11
 */