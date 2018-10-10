#include "dvb_subtitle.h"
#include <cutils/log.h>
#define LOG_TAG "dvb_subtitle"

typedef struct tagPesBufferList {
	uint8 *buf_start;						//buffer��ʼ
	uint8 *buf_end;						//buffer����
	int len;							//pes����
} PesBufList;

typedef struct tagSubtitleObject {
	int object_type;
	int provider_flag;
	int left;
	int top;
	int width;
	int height;
	int foreground;
	int background;
	int object_version;
	uint8 *character_code;
	uint8 *p_pixbuf;
	uint16 object_id;
	int pos_bitMap;
	uint16 page_id
} SubtitleObject;

typedef struct tagSubTitleCLUT {
	uint8 page_id;
	uint8 clut_version;
	uint8 clut_id;
	uint8 full_range_flag;
	uint32 *clut_2b;
	uint32 *clut_4b;
	uint32 *clut_8b;
} SubTitleCLUT;
/* The region definition gives the position of the region in a page */
typedef struct tagSubtitleRegiondef {
	uint32 pts;
	int left;
	int top;
	uint16 region_id;
	uint16 page_id;
} SubtitleRegiondef;

/* The page defines the list of regions */
typedef struct tagSubtitlePage {
	SubtitleRegiondef *region_defs;
	int numRegions;
	int numRegionsAlloc;
	uint16 page_id;
	uint16 max_regions;						//��¼���е�page�������Ҫ��ʾ��region��Ŀ
	uint8 page_time_out;						//��page����ʾʱ��,��sΪ��λ
	uint8 page_version;						//��page�İ汾
	uint8 page_state;		//0,normal_case;1,acquisition point;2,mode change
} SubtitlePage;
typedef struct tagSubtitleRegion {
	SubtitleObject *listObject;
	uint32 pts;
	int region_top;							//top
	int region_left;						//left
	int numObjects;
	int numObjectsAlloc;
	uint16 region_id;							//Ψһ��ʶһ��region
	uint16 page_id;							//��region��Ӧ��page��page_id
	uint16 region_width;						//width
	uint16 region_height;						//height
	uint8 region_version;						//�汾��
	uint8 region_level_comp;			//��ʶ��������Ҫ֧�ֵ�������͵�����/1,2bit;2,4bit;3,8bit
	uint8 region_depth;						//��������������
	uint8 clut_id;							//��ɫ���id, ��ʶʹ���ĸ���ɫ��
	uint8 entry_id;							//��ɫ���entry_id,��������Ҫ���ı�����ɫ
	uint8 background_code;					//������ı���ɫ
	uint32 fill_flag :1;						//��region�Ƿ���Ҫ�ñ���ɫ���
} SubtitleRegion;
typedef struct tagSUBTShowData {
	uint32 pts;
	int region_top;							//top
	int region_left;
	int state;
	int left;
	int top;
	int width;
	int height;
	uint16 page_id;
	uint16 region_id;
	uint16 region_width;
	uint16 region_height;
	uint8 page_state;
	uint32 fill_flag :1;
	uint8 page_time_out;						//��page����ʾʱ��,��sΪ��λ
	uint8 page_version;
	uint8 region_version;
	int numObjects;
	uint8 background_code;
	int status;
	uint8 clut_id;
	uint8 depth;
	SubtitleObject *listObject;
} SUBTShowData;
typedef struct tagSubtitle {
	SubtitlePage *p_page;
	SubtitleRegion **listRegion;
	SubTitleCLUT **listCLUT;
	SUBTShowData **listShowData;
	uint8 *pes_buf;							//���ڻ���PES����
	uint8 *bitmap_buf;						//���ڻ���bitmap����
	PesBufList pes_list[MAX_PES_NUMBER];			//������¼pes�б�
	uint8 flag_buf_used[MAX_SAVE_BITMAP_NUM]; //������Ǹ�buffer�����Ƿ�ʹ��
	uint32 filter_time;
	int filter_index;
	int numRegions;
	int numRegionsAlloc;
	int numCLUTs;
	int numCLUTsAlloc;
	int numShowDatas;
	int numShowDatasAlloc;
	int buffer_len;							//ʵ�ʷ��������pes_buf�Ĵ�С
	int pes_read;							//��ʼλ�ã�pes_buffer_list�������±�
	int pes_write;							//����λ�ã�pes_buffer_list�������±�
	int index_lang_code;					//��ǰ��Ҫ���յ���Ļ�����Ե������±꣬û����Ļ������Ϊ-1;
	uint16 elementary_PID;
	uint16 com_page_id;						//����Ļ�����page_id
	uint16 anc_page_id;						//����Ļ����ĸ���page_id
	uint8 filter_status;
	uint8 addfilter_failed_times;
	uint32 flag_display :2;					//����Ƿ���Ҫ��ʾ��Ļ,0,����Ҫ��1����Ҫ��ʾ��Ļ��2��������ʾ��Ļ

	pthread_t thread;
	uint32 prepare;
	int state;
	int fd;
} Subtitle_t;
typedef struct tagDvbSubtitleMgr {
	pthread_mutex_t mtx[1];
	Subtitle_t subtitle[MAX_SUBTITLE_NUM];
} DvbSubtitleMgr;

static int32 bit_left = 8;
static uint8 *pixel_start_ptr = NULL;
static const uint8 subt_bit_mask[9] = { 0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f,
		0x3f, 0x7f, 0xff, };
static DvbSubtitleMgr *SubtitleHandle = NULL;

void* readDara_thread(void* arg) {
	Subtitle_t* subtitle = (Subtitle_t*) arg;
	for (;;) {
		if (subtitle->state == SUB_STATE_STOPED) {
			break;
		} else if (subtitle->state == SUB_STATE_STARTED) {
			subtitle->fd;
		}
	}
	LOGI("readDara_thread end");
	return 0;
}

int DvbSubtitleMgr_init() {
	DvbSubtitleMgr *me = (DvbSubtitleMgr*) calloc(1, sizeof(DvbSubtitleMgr));
	int ret = pthread_mutex_init(&(me->mtx), 0);
	SubtitleHandle = me;
	return ret;
}

int DvbSubtitleMgr_prepare(int h, int fd) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_prepare] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->prepare == 0) {  //�Ƿ��Ѿ�prepare
		me->anc_page_id = 0xffff;
		me->com_page_id = 0xffff;
		me->pes_buf = (uint8 *) calloc(1, PES_BUFFER * 1024);	//Ԥ�ȷ�������pes����
		if (NULL == me->pes_buf) {
			return 0;
		}
		me->buffer_len = PES_BUFFER * 1024;
		me->bitmap_buf = (uint8 *) calloc(1,
		MAX_SAVE_BITMAP_NUM * BITMAP_BUFFER * 1024);		//Ԥ�ȷֳ�������bitMap
		me->fd = fd;
		me->state = SUB_STATE_INITED; //״̬
		me->prepare = 1;
	}
	return 1;
}

int DvbSubtitleMgr_start(int h) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_start] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->state == SUB_STATE_STARTED) {
		return 1;
	}
	if (me->prepare == 1) {
		me->state = SUB_STATE_STARTED;
		if (pthread_create(&(me->thread), NULL, (void *) readDara_thread, me)
				== 0) {
			return 1;
		}
	}
	return 0;
}

int DvbSubtitleMgr_stop(int h) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_start] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->state == SUB_STATE_STOPED) {
		return 1;
	}

	me->state = SUB_STATE_STOPED; //TODO

}

//�ͷ���Դ
int DvbSubtitleMgr_release(int h) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_delete] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->pes_buf) {
		free(me->pes_buf);
		me->pes_buf = NULL;
	}
	if (me->bitmap_buf) {
		free(me->bitmap_buf);
		me->bitmap_buf = NULL;
	}
	DvbSubtitleMgr_reset(h);
	return 0;
}

//�˳�������Դ
int DvbSubtitleMgr_exit() {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;
	int index = 0;
	if (handle == NULL) {
		return 1;
	}
	pthread_mutex_lock(handle->mtx);

	/*�����Ѿ�create��source*/
	for (index = 0; index < MAX_SUBTITLE_NUM; index++) {
		DvbSubtitleMgr_release(index);
	}
	/*����mutex*/
	pthread_mutex_destroy(handle->mtx);
	free(handle);
	SubtitleHandle = NULL;
	return 1;
}
/************************************************************************/
/** \brief ����object����
 * \reutrn : ��ʵ������
 */
/************************************************************************/
int DvbSubtitleMgr_delObject(SubtitleObject *p_object) {
	if (p_object) {
		if (p_object->character_code) {
			free(p_object->character_code);
			p_object->character_code = NULL;
		}
		if (p_object->p_pixbuf) {
			free(p_object->p_pixbuf);
			p_object->p_pixbuf = NULL;
		}
	}
	return 1;
}

/************************************************************************/
/** \brief ����showData����
 * \reutrn : ��ʵ������
 */
/************************************************************************/
int DvbSubtitleMgr_delShowData(SUBTShowData *p_showData) {
	int i = 0;
	SubtitleObject *p_object = NULL;
	if (p_showData) {
		if (p_showData->listObject) {
			int i = 0;
			for (i = 0; i < p_showData->numObjects; i++) {
				p_object = &p_showData->listObject[i];
				DvbSubtitleMgr_delObject(p_object);
			}
			free(p_showData->listObject);
			p_showData->listObject = NULL;
			p_showData->numObjects = 0;
		}
	}
	return 1;
}

/************************************************************************/
/** \brief ����Region����
 * \reutrn : ��ʵ������
 */
/************************************************************************/
int DvbSubtitleMgr_delRegion(SubtitleRegion *p_region) {
	SubtitleObject *p_object = NULL;
	if (p_region) {
		if (p_region->listObject) {
			int i = 0;
			for (i = 0; i < p_region->numObjects; i++) {
				p_object = &p_region->listObject[i];
				DvbSubtitleMgr_delObject(p_object);
			}
			free(p_region->listObject);
			p_region->listObject = NULL;
			p_region->numObjects = 0;
			p_region->numObjectsAlloc = 0;
		}
	}
	return 1;
}

/************************************************************************/
/** \brief �����Ӧ����Դ
 * \reutrn : ��ʵ������
 */
/************************************************************************/
int DvbSubtitleMgr_reset(int h) {
	int i = 0;
	SubtitleRegion *p_region = NULL;
	SubTitleCLUT *p_clut = NULL;
	SUBTShowData *p_showData = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_reset] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->p_page) {
		if (me->p_page->region_defs) {
			free(me->p_page->region_defs);
			me->p_page->region_defs = NULL;
			me->p_page->numRegions = 0;
			me->p_page->numRegionsAlloc = 0;
		}
		free(me->p_page);
	}
	me->p_page = NULL;
	if (me->listRegion) {
		for (i = 0; i < me->numRegions; i++) {
			p_region = me->listRegion[i];
			DvbSubtitleMgr_delRegion(p_region);
			free(p_region);
			p_region = NULL;
			me->listRegion[i] = NULL;
		}
		free(me->listRegion);
		me->listRegion = NULL;
		me->numRegions = 0;
		me->numRegionsAlloc = 0;
	}
	if (me->listCLUT) {
		for (i = 0; i < me->numCLUTs; i++) {
			p_clut = me->listCLUT[i];
			if (p_clut)
				free(p_clut);
			p_clut = NULL;
			me->listCLUT[i] = NULL;
		}
		free(me->listCLUT);
		me->listCLUT = NULL;
		me->numCLUTs = 0;
		me->numCLUTsAlloc = 0;
	}
	if (me->listShowData) {
		for (i = 0; i < me->numShowDatas; i++) {
			p_showData = me->listShowData[i];
			DvbSubtitleMgr_delShowData(p_showData);
			free(p_showData);
			p_showData = NULL;
			me->listShowData[i] = NULL;
		}
		free(me->listShowData);
		me->listShowData = NULL;
		me->numShowDatas = 0;
		me->numShowDatasAlloc = 0;
		for (i = 0; i < MAX_SAVE_BITMAP_NUM; i++) {
			me->flag_buf_used[i] = 0;
		}
		memset(me->bitmap_buf, 0, MAX_SAVE_BITMAP_NUM * BITMAP_BUFFER * 1024);
	}
	me->pes_write = 0;
	me->pes_read = 0;
	me->prepare = 0;
	me->state =SUB_STATE_INITED;
	return 1;
}

SubtitleRegion *DvbSubtitleMgr_obtainRegion(int h) {
	SubtitleRegion *subtitleRegion = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_obtainRegion] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];

	if (me->numRegions >= me->numRegionsAlloc) {
		SubtitleRegion **list = NULL;
		me->numRegionsAlloc += 2;
		list = (SubtitleRegion **) realloc(me->listRegion,
				me->numRegionsAlloc * sizeof(SubtitleRegion *));
		if (list == NULL) {
			me->numRegionsAlloc -= 2;
//			EIS_ASSERT(1);
			return NULL;
		}
		me->listRegion = list;
	}
	subtitleRegion = (SubtitleRegion *) calloc(1, sizeof(SubtitleRegion));
	me->listRegion[me->numRegions] = subtitleRegion;
	me->numRegions++;
	return subtitleRegion;
}

SubTitleCLUT *DvbSubtitleMgr_obtainCLUT(int h) {
	SubTitleCLUT *subtitleCLUT = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_obtainCLUT] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->numCLUTs >= me->numCLUTsAlloc) {
		SubTitleCLUT **list = NULL;
		me->numCLUTsAlloc += 2;
		list = (SubTitleCLUT **) realloc(me->listCLUT,
				me->numCLUTsAlloc * sizeof(SubTitleCLUT *));
		if (list == NULL) {
			me->numCLUTsAlloc -= 2;
//			EIS_ASSERT(1);
			return NULL;
		}
		me->listCLUT = list;
	}
	subtitleCLUT = (SubTitleCLUT *) calloc(1, sizeof(SubTitleCLUT));
	me->listCLUT[me->numCLUTs] = subtitleCLUT;
	me->numCLUTs++;
	return subtitleCLUT;
}

SUBTShowData *DvbSubtitleMgr_obtainShowData(int h) {
	SUBTShowData *subtitleShowData = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_obtainShowData] failed, param invalid\n",
				h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	if (me->numShowDatas >= me->numShowDatasAlloc) {
		SUBTShowData **list = NULL;
		me->numShowDatasAlloc += 6;
		list = (SUBTShowData **) realloc(me->listShowData,
				me->numShowDatasAlloc * sizeof(SUBTShowData *));
		if (list == NULL) {
			me->numShowDatasAlloc -= 6;
//			EIS_ASSERT(1);
			return NULL;
		}
		me->listShowData = list;
	}
	subtitleShowData = (SUBTShowData *) calloc(1, sizeof(SUBTShowData));
	me->listShowData[me->numShowDatas] = subtitleShowData;
	me->numShowDatas++;
	return subtitleShowData;
}

/************************************************************************/
/** \brief ����pageSegment
 * \reutrn : 0,����ʧ�ܣ�1,�����ɹ�
 */
/************************************************************************/

int DvbSubtitleMgr_parsePageSegment(int h, uint8 *buf, int len, uint32 pts,
		uint16 page_id) {
	uint8 page_time_out = 0;
	uint8 page_version = 0;
	uint8 page_state = 0;
	uint8 *ptr = buf + 2;
	int i = 0;
	int num_regions = 0;

	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_parsePage] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
//	int provider_name = 0;
//	ConfigManager *cfgmgr = ConfigManager_getHandle();
//
//	if (cfgmgr)
//		ConfigManager_getProperty(cfgmgr, CONFIG_PROP_SYSTEM_OPERATOR,
//				(int*) &provider_name);
	page_time_out = buf[0];
	page_version = ((buf[1] & 0xf0) >> 4);
	page_state = ((buf[1] & 0x0C) >> 2);
	switch (page_state) {
	/*normal case ״̬������ͬ��display set������Ҫ�����һ�ε�display set�����е�����*/
	case SUBT_PAGE_NORMAL_CASE:
		/*normal case*/
		LOGI(
				"[DvbSubtitleMgr_parsePage]id =%d , version = %d , time_out = %d NORMAL_CASE!!!!!!\n",
				page_id, page_version, page_time_out);
		break;

		/*ACQUISITION_POINT ״̬���ڲ���display set����Ҫ�����һ�ε�display set�����е�����*/
	case SUBT_PAGE_ACQUISITION_POINT:
		LOGI(
				"[DvbSubtitleMgr_parsePage]id =%d , version = %d , time_out = %d ACQUISITION_POINT!!!!!!\n",
				page_id, page_version, page_time_out);
		break;

		/*MODE_CHANGE ״̬����end of an epoch,need to reset decoder buffer,��ACQUISITION_POINT����ͬ����*/
	case SUBT_PAGE_MODE_CHANGE:
		LOGI(
				"[DvbSubtitleMgr_parsePage]id =%d , version = %d , time_out = %d MODE_CHANGE!!!!!!\n",
				page_id, page_version, page_time_out);
		break;
	}

	if (me->p_page) {
		/*��ͬ��page,��ͬ��version,regionû�з�������,ͬʱ״̬û�б�,��ֱ�ӷ���*/
		if (me->p_page->page_id
				== page_id&& me->p_page->page_version == page_version && page_state == SUBT_PAGE_NORMAL_CASE) {
			return 0;
		}
	} else {
		/* new page */
		me->p_page = (SubtitlePage *) calloc(1, sizeof(SubtitlePage));
		if (NULL == me->p_page) {
			LOGE(
					"[DvbSubtitleMgr_parsePage]calloc page memory failed !!!!!!!!!\n");
			return 0;
		}
	}
	if ((len - 2) % 6 != 0) {
		LOGE("[DvbSubtitleMgr_parsePage] wrong segment_length = %d !!!!!!!!!\n",
				len);
		return 0;
	}

	/************************************************************************/
	/*����������ʾ��region�ĸ���������������region��pes����������region��һ���Ӽ�,	*/
	/*ֻ�����������Ĳ���Ҫ��ʾ														*/
	/************************************************************************/
	num_regions = (len - 2) / 6;
	if (0 == num_regions) {
		memset(me->p_page->region_defs, 0,
				me->p_page->numRegionsAlloc * sizeof(SubtitleRegiondef));
		me->p_page->numRegions = 0;
		me->p_page->page_state = page_state;
		me->p_page->page_id = page_id;
		me->p_page->page_version = page_version;
		me->p_page->page_time_out = page_time_out;

		LOGI("[DvbSubtitleMgr_parsePage] no region number !!!!!!!!!\n");
		return 0;
	}

	/************************************************************************/
	/*����Ϊ�˱���ÿ��calloc��free�ڴ棬һ�ν������Ⱦͷֳ�8��region,			      */
	/*�����Ҫ��ʾ�Ĵ���8��region����������[һ������²���һ����Ҫͬʱ��ʾ8��region]  */
	/************************************************************************/
	if (me->p_page->numRegionsAlloc == 0) {
		SubtitleRegiondef *list = NULL;
		if (num_regions > 8) {
			me->p_page->numRegionsAlloc = num_regions;
		} else
			me->p_page->numRegionsAlloc = 8;
		list = (SubtitleRegiondef *) realloc(me->p_page->region_defs,
				me->p_page->numRegionsAlloc * sizeof(SubtitleRegiondef));
		if (NULL == list) {
			LOGE(
					"[DvbSubtitleMgr_parsePage]calloc page region_defs memory failed!\n");
			me->p_page->numRegions = 0;
			me->p_page->numRegionsAlloc = 0;
			return 0;
		}
		memset(list, 0,
				me->p_page->numRegionsAlloc * sizeof(SubtitleRegiondef));
		me->p_page->region_defs = list;
	} else {
		/*���region_defs�ɵ�����*/
		memset(me->p_page->region_defs, 0,
				me->p_page->numRegionsAlloc * sizeof(SubtitleRegiondef));
		if (num_regions > me->p_page->numRegionsAlloc) {
			SubtitleRegiondef *list = NULL;
			list = (SubtitleRegiondef *) realloc(me->p_page->region_defs,
					num_regions * sizeof(SubtitleRegiondef));
			if (NULL == list) {
				LOGE(
						"[DvbSubtitleMgr_parsePage]calloc page region_defs memory failed!\n");
				me->p_page->numRegions = 0;
				return 0;
			}
			memset(list, 0, num_regions * sizeof(SubtitleRegiondef));
			me->p_page->numRegionsAlloc = num_regions;
			me->p_page->region_defs = list;
		}
	}
	me->p_page->numRegions = num_regions;
	me->p_page->page_id = page_id;
	me->p_page->page_version = page_version;
	me->p_page->page_time_out = page_time_out;
	me->p_page->page_state = page_state;
	/************************************************************************/
	/* ����Ϊpage ��������region��������Ϣ�����������mode_change ״̬��region��width */
	/* height���ᷢ�����£�������Ҫ��ʾ������ᷢ�����£���page��������				  */
	/************************************************************************/
	for (i = 0; i < me->p_page->numRegions; i++) {
		me->p_page->region_defs[i].region_id = *ptr++;
		ptr++;
		GET16(me->p_page->region_defs[i].left, ptr);
		GET16(me->p_page->region_defs[i].top, ptr);
//		if (provider_name == OPERATOR_TAIWAN) {		//̨����Ļ������ƫ�󣬵�����ʾ���ˣ���������������һ��
//			me->p_page->region_defs[i].left -= 513;
//			me->p_page->region_defs[i].top -= 542;
//		}
		me->p_page->region_defs[i].pts = pts;
		LOGI("[DvbSubtitleMgr_parsePage]region_id =%d , left = %d,top = %d\n",
				me->p_page->region_defs[i].region_id,
				me->p_page->region_defs[i].left,
				me->p_page->region_defs[i].top);
	}

	return 1;
}

/************************************************************************/
/** \brief ����RegionSegment
 * \reutrn : 0,����ʧ�ܣ�1,�����ɹ�
 */
/************************************************************************/
int DvbSubtitleMgr_parseRegionSegment(int h, uint8 *buf, int len,
		uint16 page_id) {
	uint16 region_id = 0;
	uint8 region_version = 0;
	uint8 fill_flag = 0;
	uint8 region_8bit_pixel_code = 0;
	uint8 region_4bit_pixel_code = 0;
	uint8 region_2bit_pixel_code = 0;
	uint8 object_type = 0;
	uint8 *ptr = buf + 10;
	uint8 *pBuffer = ptr;
	uint8 *end_ptr = NULL;
	uint16 numObjects = 0;
	int i = 0;
	SubtitleRegion *p_region = NULL;
	SubtitleObject *p_object = NULL;
	region_id = buf[0];
	region_version = ((buf[1] & 0xf0) >> 4);
	fill_flag = ((buf[1] & 0x08) >> 3);
	end_ptr = buf + len;

	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE(
				"h = %s [DvbSubtitleMgr_parseRegionSegment] failed, param invalid\n",
				h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	/************************************************************************/
	/* �����Ƿ��Ѿ����ڸ�region������Ѿ�������ֱ�Ӹ����µ���Ϣ������Ϊ�µ�region,����  */
	/* ���뵽region������������regionΪһ�� epoch������Ҫ��ʾ�����е�region��Ϣ,��������*/
	/* ���������ľ���Ҫ��ʾ��ÿ��display setֻ������ʾpage��������						*/
	/************************************************************************/
	for (i = 0; i < me->numRegions; i++) {
		p_region = me->listRegion[i];
		if (p_region->region_id == region_id) {
			if (p_region->region_version == region_version) {
				return 0;
			}
			break;
		}
	}
	/* ��region */
	if (i == me->numRegions) {
		p_region = NULL;
		p_region = DvbSubtitleMgr_obtainRegion(me);
		if (NULL == p_region) {
			LOGE(
					"[DvbSubtitleMgr_parseRegionSegment]calloc region memory failed !!!!!!!!!!!!!!\n");
			return 0;
		}
	}

	/* ����region���� */
	p_region->page_id = page_id;
	p_region->region_id = region_id;
	p_region->region_version = region_version;
	p_region->fill_flag = fill_flag;
	p_region->region_width = (buf[2] << 8) | buf[3];
	p_region->region_height = (buf[4] << 8) | buf[5];
	p_region->region_level_comp = ((buf[6] & 0xE0) >> 5);
	p_region->region_depth = ((buf[6] & 0x1C) >> 2);
	p_region->clut_id = buf[7];
	region_8bit_pixel_code = buf[8];
	region_4bit_pixel_code = ((buf[9] & 0xF0) >> 4);
	region_2bit_pixel_code = ((buf[9] & 0x0C) >> 2);

	/*region�ı�����ɫ*/
	if (fill_flag) {
		p_region->background_code =
				((1 == p_region->region_depth) ?
						region_2bit_pixel_code :
						((2 == p_region->region_depth) ?
								region_4bit_pixel_code : region_8bit_pixel_code));
	}
	/*�����region��object��Ŀ*/
	while (pBuffer < end_ptr) {
		object_type = (pBuffer[2] & 0xC0) >> 6;
		if ((SUBT_OBJECT_CHARACTER == object_type)
				|| (SUBT_OBJECT_STRING == object_type)) {
			pBuffer += 8;
		} else {
			pBuffer += 6;
		}
		numObjects++;
	}

	/************************************************************************/
	/*����Ϊ�˱���ÿ��calloc��free�ڴ棬һ�ν������Ⱦͷֳ�8��object,			 */
	/*���һ��region�а�������8��object����������[һ������²�����֣����Ǳ�̬]  */
	/************************************************************************/
	if (p_region->numObjectsAlloc == 0) {
		SubtitleObject *list = NULL;
		if (numObjects > 8) {
			p_region->numObjectsAlloc = numObjects;
		} else
			p_region->numObjectsAlloc = 8;
		list = (SubtitleObject *) realloc(p_region->listObject,
				p_region->numObjectsAlloc * sizeof(SubtitleObject));
		if (NULL == list) {
			LOGE(
					"[DvbSubtitleMgr_parsePageSegment]calloc listObject memory failed!\n");
			p_region->numObjects = 0;
			p_region->numObjectsAlloc = 0;
			return 0;
		}
		memset(list, 0, p_region->numObjectsAlloc * sizeof(SubtitleObject));
		p_region->listObject = list;
	} else {
		/*��region �����˸��£��������old object*/
		int i = 0;
		for (i = 0; i < p_region->numObjects; i++) {
			p_object = &p_region->listObject[i];
			if (p_object->character_code) {
				free(p_object->character_code);
				p_object->character_code = NULL;
			}
			if (p_object->p_pixbuf) {
				free(p_object->p_pixbuf);
				p_object->p_pixbuf = NULL;
			}
		}
		memset(p_region->listObject, 0,
				p_region->numObjectsAlloc * sizeof(SubtitleObject));
		if (numObjects > p_region->numObjectsAlloc) {
			SubtitleObject *list = NULL;
			list = (SubtitleObject *) realloc(p_region->listObject,
					numObjects * sizeof(SubtitleObject));
			if (NULL == list) {
				LOGE(
						"[DvbSubtitleMgr_parsePageSegment]calloc listObject memory failed!\n");
				p_region->numObjects = 0;
				return 0;
			}
			memset(list, 0, numObjects * sizeof(SubtitleObject));
			p_region->numObjectsAlloc = numObjects;
			p_region->listObject = list;
		}
	}
	p_region->numObjects = numObjects;

	LOGI(
			"[DvbSubtitleMgr_parseRegion]id =%d , version = %d , numObjects = %d , width = %d , height = %d\n",
			region_id, region_version, numObjects, p_region->region_width,
			p_region->region_height);

	if (numObjects > 0) {
		for (i = 0; i < p_region->numObjects; i++) {
			p_object = &p_region->listObject[i];
			GET16(p_object->object_id, ptr);
			p_object->object_type = ((ptr[0] & 0xC0) >> 6);
			p_object->provider_flag = ((ptr[0] & 0x30) >> 4);
			p_object->left = ((ptr[0] & 0x0F) << 8) | ptr[1];
			ptr += 2;
			p_object->top = ((ptr[0] & 0x0F) << 8) | ptr[1];
			ptr += 2;
			if ((SUBT_OBJECT_CHARACTER == p_object->object_type)
					|| (SUBT_OBJECT_STRING == p_object->object_type)) {
				p_object->foreground = *ptr++;
				p_object->background = *ptr++;
			}
			p_object->object_version = 0xff;
			LOGI("[DvbSubtitleMgr_parseRegion]object_id =%d,left= %d,top=%d\n",
					p_object->object_id, p_object->left, p_object->top);
		}
	}
	return 1;
}

/************************************************************************/
/** \brief ����CLUTSegment
 * \reutrn : 0,����ʧ�ܣ�1,�����ɹ�
 */
/************************************************************************/
int DvbSubtitleMgr_parseCLUTSegment(int h, uint8 *buf, int len, uint16 paeg_id) {
	int i = 0;
	SubTitleCLUT *p_clut = NULL;
	uint8 clut_id = 0;
	uint8 clut_version = 0;
	uint8 *ptr = buf + 2;
	uint8 *end_ptr = NULL;
	uint8 entry_id = 0;
	uint8 entry_flag = 0;
	uint32 yuv_value = 0;
	clut_id = buf[0];
	clut_version = (buf[1] & 0xF0) >> 4;
	end_ptr = buf + len;

	LOGI("[DvbSubtitleMgr_parseCLUT]id =%d , version = %d\n", clut_id,
			clut_version);

	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_parseCLUT] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];

	// check if we already have this clut
	for (i = 0; i < me->numCLUTs; i++) {
		p_clut = me->listCLUT[i];
		if (p_clut->clut_id == clut_id)   // already have this CLUT
				{
			if (p_clut->clut_version == clut_version) // same contents of this CLUT segment
					{
				return 0;
			}
			break;
		}
	}

	if (i == me->numCLUTs) { //new clut
		p_clut = DvbSubtitleMgr_obtainCLUT(me);
		if (NULL == p_clut) {
			//��ʾ
			return 0;
		}
	}
	p_clut->page_id = paeg_id;
	p_clut->clut_version = clut_version;
	p_clut->clut_id = clut_id;
	while (ptr < end_ptr) {
		uint8 y, cr, cb, t;
		entry_id = *ptr++;
		entry_flag = ((ptr[0] & 0xE0) >> 5);
		p_clut->full_range_flag = (ptr[0] & 0x01);
		ptr++;
		if (1 == p_clut->full_range_flag) // full 8-bit resolution
				{
			y = *ptr++;
			cr = *ptr++;
			cb = *ptr++;
			t = *ptr++;
		} else {
			y = (ptr[0] & 0xFC) >> 2;
			cr = ((ptr[0] & 0x03) << 2) | ((ptr[1] & 0xC0) >> 2);
			cb = (ptr[1] & 0x3C) >> 2;
			t = (ptr[1] & 0x03);
			ptr += 2;
		}

		if (0 == y) {
			t = 0;
		} else {
			t = 0xff - t;
		}

		// [T,Cr,Cb,Y]
		yuv_value = ((t << 24) | (cr << 16) | (cb << 8) | (y));

		if (entry_flag & 0x04) {
			p_clut->clut_2b[entry_id] = yuv_value;
		}

		if (entry_flag & 0x02) {
			p_clut->clut_4b[entry_id] = yuv_value;
		}

		if (entry_flag & 0x01) {
			p_clut->clut_8b[entry_id] = yuv_value;
		}
	}
	return 1;
}

/************************************************************************/
/** \brief ��һ����ַ��ȡ����byte����,ָ����֮����[��Ҫ���ڽ���object��ʱ��ѭ��
 ����������λ]
 * \reutrn : ��ȡ������
 */
/************************************************************************/
uint8 sub_bit_read(uint8 bit_count) {
	uint8 bit_result = 0;

	while (bit_count > 0) {
		if (bit_left - bit_count >= 0) {
			bit_result = bit_result
					| ((*pixel_start_ptr >> (bit_left - bit_count))
							& subt_bit_mask[bit_count]);
			bit_left -= bit_count;
			if (0 == bit_left) {
				pixel_start_ptr++;
				bit_left = 8;
			}
			bit_count = 0;
		} else {
			bit_result = bit_result
					| ((*pixel_start_ptr & subt_bit_mask[bit_left])
							<< (bit_count - bit_left));
			bit_count -= bit_left;
			pixel_start_ptr++;
			bit_left = 8;
		}
	}
	return bit_result;
}

/************************************************************************/
/** \brief ����2λ ��ɫ��ȵ�object
 pixbuf_start : buffer��ַ
 width : �ɼ���ó���bitmap�ÿ��
 pixbuf_offset : ƫ����
 * \reutrn : ������
 */
/************************************************************************/
int SUBT_Decode_Object_2bit_Pixel_Code(uint8 *pixbuf_start, uint16 width,
		uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	uint8 switch_1 = 0, switch_2 = 0, switch_3 = 0;
	while ((!string_end)) {
		pixel_count = pixel_color = 0;
		pixel_color = sub_bit_read(2); //((ptr[0] & 0xC0)>>6);
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1); //((ptr[0] & 0x20)>>5);

			if (0x01 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(3) + 3; //((ptr[0] & 0x1C)>>2) + 3;
				pixel_color = sub_bit_read(2); //(ptr[0] & 0x03);
			} else {
				switch_2 = sub_bit_read(1); //(ptr[0] & 0x10)>>4 ;
				if (0x00 == switch_2) // switch2
						{
					switch_3 = sub_bit_read(2); //(ptr[0] & 0x0C)>>2 ;
					switch (switch_3) // switch3
					{
					case 0x00: // end of 2-bit/pixel_code_string
						string_end = 1;
						break;
					case 0x01: // two pixels shall be set to pseudo colour (entry) '00'
						pixel_count = 2;
						break;
					case 0x02: // the following 6 bits contain run length coded pixel data
						pixel_count = sub_bit_read(4) + 12; //(((ptr[0] & 0x03)<<2) & ((ptr[1] &0xC0)>>6)) + 12;
						pixel_color = sub_bit_read(2); //(ptr[1] & 0x30)>>4;
						break;
					case 0x03: // the following 10 bits contain run length coded pixel data
						pixel_count = sub_bit_read(8) + 29; //(((ptr[0] & 0x03)<<6) & ((ptr[1] & 0xC0)>>2)) + 29;
						pixel_color = sub_bit_read(2); //(ptr[1] & 0x03);
						break;
					default:
						break;
					}
				} else // signals that one pixel shall be set to pseudo-colour (entry) '00',
				{
					pixel_count = 1;
				}
			}
		}

		if (pixel_count + *pixbuf_offset > width) {
			//pixel_count = width - (*pixbuf_offset);
			//��ʾ����
			break;
		}

		memset(pixbuf_start + *pixbuf_offset, pixel_color, pixel_count);

		(*pixbuf_offset) += pixel_count;
	}

	return 1;
}

/************************************************************************/
/** \brief ��ȡ��������bitmap����һ�е�ƫ����[ʵ��Ϊwidth]
 pixbuf_start : buffer��ַ
 * \reutrn : ������pix������
 */
/************************************************************************/
int GET_2bit_Pixel_Code_LEN(uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	uint8 switch_1 = 0, switch_2 = 0, switch_3 = 0;
	while ((!string_end)) {
		pixel_count = pixel_color = 0;
		pixel_color = sub_bit_read(2);			//((ptr[0] & 0xC0)>>6);
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1);			//((ptr[0] & 0x20)>>5);

			if (0x01 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(3) + 3; //((ptr[0] & 0x1C)>>2) + 3;
				pixel_color = sub_bit_read(2); //(ptr[0] & 0x03);
			} else {
				switch_2 = sub_bit_read(1); //(ptr[0] & 0x10)>>4 ;
				if (0x00 == switch_2) // switch2
						{
					switch_3 = sub_bit_read(2); //(ptr[0] & 0x0C)>>2 ;
					switch (switch_3) // switch3
					{
					case 0x00: // end of 2-bit/pixel_code_string
						string_end = 1;
						break;
					case 0x01: // two pixels shall be set to pseudo colour (entry) '00'
						pixel_count = 2;
						break;
					case 0x02: // the following 6 bits contain run length coded pixel data
						pixel_count = sub_bit_read(4) + 12; //(((ptr[0] & 0x03)<<2) & ((ptr[1] &0xC0)>>6)) + 12;
						pixel_color = sub_bit_read(2); //(ptr[1] & 0x30)>>4;
						break;
					case 0x03: // the following 10 bits contain run length coded pixel data
						pixel_count = sub_bit_read(8) + 29; //(((ptr[0] & 0x03)<<6) & ((ptr[1] & 0xC0)>>2)) + 29;
						pixel_color = sub_bit_read(2); //(ptr[1] & 0x03);
						break;
					default:
						break;
					}
				} else // signals that one pixel shall be set to pseudo-colour (entry) '00',
				{
					pixel_count = 1;
				}
			}
		}
		(*pixbuf_offset) += pixel_count;
	}
	return 1;
}

/************************************************************************/
/** \brief ����4λ ��ɫ��ȵ�object
 pixbuf_start : buffer��ַ
 width : �ɼ���ó���bitmap�ÿ��
 pixbuf_offset : ƫ����
 * \reutrn : ������
 */
/************************************************************************/
int SUBT_Decode_Object_4bit_Pixel_Code(uint8 *pixbuf_start, uint16 width,
		uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	int i = 0;
	uint8 switch_1 = 0, switch_2 = 0, switch_3 = 0;

	while (!string_end) {
		pixel_count = pixel_color = 0;

		pixel_color = sub_bit_read(4); //((ptr[0] & 0xF0) >> 4);
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1); //((ptr[0] & 0x08) >> 3);
			if (0x00 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(3); //(ptr[0] & 0x07);
				if (0 == pixel_count) {
					string_end = 1;
				} else // Number of pixels minus 2 that shall be set to pseudo-colour (entry) '0000'.
				{
					pixel_count += 2;
				}
			} else {
				switch_2 = sub_bit_read(1); //((ptr[0] & 0x04) >> 2);
				if (0x00 == switch_2) // switch2
						{
					pixel_count = sub_bit_read(2) + 4; //(ptr[0] & 0x03) + 4;
					//ptr++;
					pixel_color = sub_bit_read(4);	//((ptr[0] & 0xF0) >> 4);
				} else {
					switch_3 = sub_bit_read(2);				//(ptr[0] & 0x03) ;
					//ptr++;
					switch (switch_3) // switch3
					{
					case 0x00: // 1 pixel shall be set to pseudo-colour (entry) '0000'
						pixel_count = 1;
						break;
					case 0x01: // 2 pixels shall be set to pseudo-colour (entry) '0000'
						pixel_count = 2;
						break;
					case 0x02: // the following 8 bits contain run-length coded pixel-data
						pixel_count = sub_bit_read(4) + 9; //((ptr[0] & 0xF0) >> 4) + 9;
						pixel_color = sub_bit_read(4); //(ptr[0] & 0x0F);
						break;
					case 0x03: // the following 12 bits contain run-length coded pixel-data
						pixel_count = sub_bit_read(8) + 25; //(ptr[0] & 0xFF) + 25;
						//	ptr++;
						pixel_color = sub_bit_read(4);//((ptr[0] & 0xF0) >> 4);
						break;
					default:
						break;
					}
				}
			}
		}

		if (pixel_count + *pixbuf_offset > width) {
			//pixel_count = width - (*pixbuf_offset);
			//��ʾ����
			break;
		}
		memset(pixbuf_start + *pixbuf_offset, pixel_color, pixel_count);
		(*pixbuf_offset) += pixel_count;
	}

	return 1;
}

/************************************************************************/
/** \brief ��ȡ��������bitmap����һ�е�ƫ����[ʵ��Ϊwidth]
 pixbuf_start : buffer��ַ
 * \reutrn : ������pix������
 */
/************************************************************************/
int GET_4bit_Pixel_Code_LEN(uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	int i = 0;
	uint8 switch_1 = 0, switch_2 = 0, switch_3 = 0;

	while (!string_end) {
		pixel_count = pixel_color = 0;

		pixel_color = sub_bit_read(4);			 //((ptr[0] & 0xF0) >> 4);
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1);			 //((ptr[0] & 0x08) >> 3);
			if (0x00 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(3); //(ptr[0] & 0x07);
				if (0 == pixel_count) {
					string_end = 1;
				} else // Number of pixels minus 2 that shall be set to pseudo-colour (entry) '0000'.
				{
					pixel_count += 2;
				}
			} else {
				switch_2 = sub_bit_read(1); //((ptr[0] & 0x04) >> 2);
				if (0x00 == switch_2) // switch2
						{
					pixel_count = sub_bit_read(2) + 4; //(ptr[0] & 0x03) + 4;
					//ptr++;
					pixel_color = sub_bit_read(4);	//((ptr[0] & 0xF0) >> 4);
				} else {
					switch_3 = sub_bit_read(2);				//(ptr[0] & 0x03) ;
					//ptr++;
					switch (switch_3) // switch3
					{
					case 0x00: // 1 pixel shall be set to pseudo-colour (entry) '0000'
						pixel_count = 1;
						break;
					case 0x01: // 2 pixels shall be set to pseudo-colour (entry) '0000'
						pixel_count = 2;
						break;
					case 0x02: // the following 8 bits contain run-length coded pixel-data
						pixel_count = sub_bit_read(4) + 9; //((ptr[0] & 0xF0) >> 4) + 9;
						pixel_color = sub_bit_read(4); //(ptr[0] & 0x0F);
						break;
					case 0x03: // the following 12 bits contain run-length coded pixel-data
						pixel_count = sub_bit_read(8) + 25; //(ptr[0] & 0xFF) + 25;
						//	ptr++;
						pixel_color = sub_bit_read(4);//((ptr[0] & 0xF0) >> 4);
						break;
					default:
						break;
					}
				}
			}
		}
		(*pixbuf_offset) += pixel_count;
	}
	return 1;
}

/************************************************************************/
/** \brief ����8λ ��ɫ��ȵ�object
 pixbuf_start : buffer��ַ
 width : �ɼ���ó���bitmap�ÿ��
 pixbuf_offset : ƫ����
 * \reutrn : ������
 */
/************************************************************************/
int SUBT_Decode_Object_8bit_Pixel_Code(uint8 *pixbuf_start, uint16 width,
		uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	int i = 0;
	uint8 switch_1 = 0;

	while (!string_end) {
		pixel_count = pixel_color = 0;
		pixel_color = sub_bit_read(8);						//*ptr++ ;
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1);					//((ptr[0] & 0x80)>>7);
			if (0 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(7); //(ptr[0] & 0x7F);
				if (0 == pixel_count) {
					string_end = 1;
				} else // Number of pixels that shall be set to pseudo-colour (entry) '0x00'
				{
					pixel_count = pixel_count;
				}
			} else { // Number of pixels that shall be set to the pseudo-colour defined next.
					 // This field shall not have a value of less than three.
				pixel_count = sub_bit_read(7);				//(ptr[0] & 0x7F);
				pixel_color = sub_bit_read(8);				//*ptr++;
			}
		}
		if (pixel_count + *pixbuf_offset > width) {
			//pixel_count = width - (*pixbuf_offset);
			//��ʾ����
			break;
		}

		memset(pixbuf_start + *pixbuf_offset, pixel_color, pixel_count);

		(*pixbuf_offset) += pixel_count;
	}

	return 1;
}

/************************************************************************/
/** \brief ��ȡ��������bitmap����һ�е�ƫ����[ʵ��Ϊwidth]
 pixbuf_start : buffer��ַ
 * \reutrn : ������pix������
 */
/************************************************************************/
int GET_8bit_Pixel_Code_LEN(uint16 *pixbuf_offset) {
	uint16 pixel_count = 0, pixel_color = 0;
	int string_end = 0;
	int i = 0;
	uint32 switch_1 = 0;

	while (!string_end) {
		pixel_count = pixel_color = 0;
		pixel_color = sub_bit_read(8);			//*ptr++ ;
		if (pixel_color != 0x00) {
			pixel_count = 1;
		} else {
			switch_1 = sub_bit_read(1);			//((ptr[0] & 0x80)>>7);
			if (0 == switch_1) // switch1
					{
				pixel_count = sub_bit_read(7); //(ptr[0] & 0x7F);
				if (0 == pixel_count) {
					string_end = 1;
				} else // Number of pixels that shall be set to pseudo-colour (entry) '0x00'
				{
					pixel_count = pixel_count;
				}
			} else { // Number of pixels that shall be set to the pseudo-colour defined next.
					 // This field shall not have a value of less than three.
				pixel_count = sub_bit_read(7);				//(ptr[0] & 0x7F);
				pixel_color = sub_bit_read(8);				//*ptr++;
			}
		}
		(*pixbuf_offset) += pixel_count;
	}
	return 1;
}

/************************************************************************/
/** \brief ����������BitMap,����õ���Ӧ��widht�Լ�height
 * \reutrn : ������
 */
/************************************************************************/
void DvbSubtitleMgr_getBitMapPix(uint8 *field_addr, uint16 data_len,
		uint16 *width, uint16 *height) {
	uint8 data_type = 0;
	uint16 pixbuf_offset = 0;
	uint8 *ptr = field_addr;
	uint8 *end_ptr = ptr + data_len;
	if (data_len == 0) {
		LOGE(
				"[SUBT_Decode_Object_Pixel_Data]wrong  data_len = 0 !!!!!!!!!!!! \n");
		return;
	}

	while (ptr < end_ptr) {
		data_type = *ptr++;
		pixel_start_ptr = ptr;
		switch (data_type) {
		case SUBT_BLOCK_2BIT_CODE_STRING: //0x10
			GET_2bit_Pixel_Code_LEN(&pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_4BIT_CODE_STRING: //0x11
			GET_4bit_Pixel_Code_LEN(&pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_8BIT_CODE_STRING: //0x12
			GET_8bit_Pixel_Code_LEN(&pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_2TO4BIT_MAP_TABLE: //0x20
		case SUBT_BLOCK_2TO8BIT_MAP_TABLE: //0x21
		case SUBT_BLOCK_4TO8BIT_MAP_TABLE: //0x22
			break;

		case SUBT_BLOCK_END_OF_OBJECT_LINE: //0xf0
			if (*width != 0 && *width != pixbuf_offset) {
				//��ʾ���β��ˣ���Ȼ���ν���������width��һ��!!!!!!!!!!!!!!!!
				LOGE(
						"[DvbSubtitleMgr_getObjectWidth]parse width wrong!!!!!!!! width = %d , pixbuf_offset = %d\n",
						*width, pixbuf_offset);
				if (*width != 0 && *width < pixbuf_offset) {
					*width = pixbuf_offset;
				}
			} else {
				*width = pixbuf_offset;
			}
			pixbuf_offset = 0;
			*height += 1;
			break;
		}
	}
}

/************************************************************************/
/** \brief ����bitMap
 * \reutrn : ������
 */
/************************************************************************/
int SUBT_Decode_Object_Pixel_Data(Subtitle_t *me, SubtitleObject *p_object,
		uint8 *field_addr, uint16 data_len, uint16 width, int flag) {
	uint8 *p_pixbuf = NULL;
	uint8 data_type = 0;
	uint16 pixbuf_offset = 0;
	uint8 *ptr = field_addr;
	uint8 *end_ptr = ptr + data_len;

	if (data_len == 0) {
		LOGE(
				"[SUBT_Decode_Object_Pixel_Data]wrong  data_len = 0 !!!!!!!!!!!! \n");
		return 0;
	}

	if (p_object->pos_bitMap == -1) {
		p_pixbuf = p_object->p_pixbuf + flag * width;
	} else {
		p_pixbuf =
				(me->bitmap_buf + p_object->pos_bitMap * BITMAP_BUFFER * 1024)
						+ flag * width;
	}

	while (ptr < end_ptr) {
		data_type = *ptr++;
		pixel_start_ptr = ptr;
		switch (data_type) {
		case SUBT_BLOCK_2BIT_CODE_STRING: //0x10
			SUBT_Decode_Object_2bit_Pixel_Code(p_pixbuf, width, &pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_4BIT_CODE_STRING: //0x11
			SUBT_Decode_Object_4bit_Pixel_Code(p_pixbuf, width, &pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_8BIT_CODE_STRING: //0x12
			SUBT_Decode_Object_8bit_Pixel_Code(p_pixbuf, width, &pixbuf_offset);
			if (bit_left != 8) {
				ptr = pixel_start_ptr + 1;
				bit_left = 8;
			} else {
				ptr = pixel_start_ptr;
			}
			break;

		case SUBT_BLOCK_2TO4BIT_MAP_TABLE: //0x20
		case SUBT_BLOCK_2TO8BIT_MAP_TABLE: //0x21
		case SUBT_BLOCK_4TO8BIT_MAP_TABLE: //0x22
			break;

		case SUBT_BLOCK_END_OF_OBJECT_LINE: //0xf0
			p_pixbuf += 2 * width;
			pixbuf_offset = 0;
			break;
		}
	}

	if (ptr >= end_ptr && (data_type != SUBT_BLOCK_END_OF_OBJECT_LINE)) {
		//��ʾ
	}

	return 1;
}
/************************************************************************/
/** \brief ����page_id�Լ�object_id ��ȡobject ����
 * \reutrn : object����
 */
/************************************************************************/
SubtitleObject *DvbSubtitleMgr_getObject(int h, uint16 page_id,
		uint16 object_id) {
	SubtitleRegion *p_region = NULL;
	SubtitleObject *p_object = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_getObject] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	int i = 0;
	for (i = 0; i < me->numRegions; i++) {
		int j = 0;
		p_region = me->listRegion[i];
		for (j = 0; j < p_region->numObjects; j++) {
			p_object = &p_region->listObject[i];
			if (page_id == p_region->page_id
					&& p_object->object_id == object_id) {
				return p_object;
			}
		}
	}
	return p_object;
}

/************************************************************************/
/** \brief ��ȡ��һ�����е�bitMap��buffer
 * \reutrn :-1,��ȡʧ�ܣ�����buffer���ڱ��ã�>=0,����buffer��һ�������±�
 */
/************************************************************************/
int DvbSubtitleMgr_getFreeBitMapBuffer(int h) {
	int i = 0;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE(
				"h = %s [DvbSubtitleMgr_getFreeBitMapBuffer] failed, param invalid\n",
				h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	for (i = 0; i < MAX_SAVE_BITMAP_NUM; i++) {
		if (me->flag_buf_used[i] == 0) {
			return i;
		}
	}
	return -1;
}

/************************************************************************/
/** \brief ����ObjectSegment
 * \reutrn : 0,����ʧ�ܣ�1,�����ɹ�
 */
/************************************************************************/
int DvbSubtitleMgr_parseObjectSegment(int h, uint8 *buf, int len,
		uint16 page_id) {
	uint16 object_id = 0;
	uint8 object_version = 0;
	uint8 object_coding_method = 0;
	uint8 non_modifying_colour_flag = 0;
	uint8 *ptr = buf;
	SubtitleRegion *p_region = NULL;
	SubtitleObject *p_object = NULL;
	int i = 0;

	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_parseObject] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	GET16(object_id, ptr);

	object_version = ((ptr[0] & 0xF0) >> 4);
	object_coding_method = ((ptr[0] & 0x08) >> 2);
	non_modifying_colour_flag = ((ptr[0] & 0x02) >> 1);
	ptr++;
	/*��ѯ��object�Ƿ���ĳ��region�е�object*/

	LOGI("[DvbSubtitleMgr_parseObject]object_id = %d , version = %d\n",
			object_id, object_version);

	for (i = 0; i < me->numRegions; i++) {
		int j = 0;
		p_region = me->listRegion[i];
		for (j = 0; j < p_region->numObjects; j++) {
			p_object = &p_region->listObject[j];
			if (page_id == p_region->page_id
					&& p_object->object_id == object_id) {
				break;
			}
		}
		if (j < p_region->numObjects)
			break;
	}

	if (i < me->numRegions) {
		if (p_object->object_version == object_version) { //��objectû�з������£��������
			return 0;
		} else {
			p_object->object_version = object_version;
			p_object->page_id = page_id;
			/*objectЯ���ַ���Ϣ*/
			if (SUBT_OBJECT_CODING_CHARACTER == object_coding_method) {
				uint8 *data_ptr = buf + 3;
				uint8 num_codes = *data_ptr++;
				if (p_object->character_code) {
					free(p_object->character_code);
					p_object->character_code = NULL;
				}
				p_object->character_code = (uint8 *) calloc(1,
						num_codes * 2 + 1);
				if (NULL == p_object->character_code) {
					LOGE(
							"[DvbSubtitleMgr_parseObjectSegment]calloc object character_code memory failed !!!!!!!!!\n");
				}
				strncpy((char *) p_object->character_code, (char *) data_ptr,
						num_codes * 2);
				LOGE(
						"[DvbSubtitleMgr_parseObjectSegment]SUBT_OBJECT_CODING_CHARACTER !!!!!!!!!\n");

				return 1;
			}
			/*objectЯ��λͼ��Ϣ*/
			else if (SUBT_OBJECT_CODING_PIXEL == object_coding_method) {
				uint16 topfield_len = 0;
				uint16 bottomfield_len = 0;
				uint8 *data_ptr = NULL;
				int flag = 0;
				uint16 width = 0, height = 0;
				GET16(topfield_len, ptr);
				GET16(bottomfield_len, ptr);
				data_ptr = buf + 7;
				if (topfield_len == 0) {
					LOGE(
							"[DvbSubtitleMgr_parseObjectSegment]wrong topfield_len = 0 !!!!!!!!!\n");
					return 0;
				}
				DvbSubtitleMgr_getBitMapPix(data_ptr, topfield_len, &width,
						&height); //ȡwidth
				if (bottomfield_len > 0) {
					DvbSubtitleMgr_getBitMapPix(data_ptr, bottomfield_len,
							&width, &height); //ȡheight
				} else {
					DvbSubtitleMgr_getBitMapPix(data_ptr, topfield_len, &width,
							&height); //ȡheight
				}
				p_object->width = width;
				p_object->height = height;

				/************************************************************************/
				/* ����bitmapͼƬ�Ĵ�С[����ֻ�Ǽ�¼��bitmap��Ӧ��clut��entry_id,ת��Ϊrgb8888 */
				/* ����ʾ��ʱ��ת��,���������˷ѵĿռ�С] , �������Ԥ�ȷ����BITMAP_BUFFER�Ĵ�С*/
				/* ����Ҫ�·���һ�������<BITMAP_BUFFER,����Ԥ�ȷֳ�����buffer�в���һ�����еĶ� */
				/* ��buffer�����������·��䣬��ʾ��Ϻ�free*/
				/************************************************************************/
				if (width * height > BITMAP_BUFFER * 1024) { //��bitmapͼƬ������16K��û������������buffer�������ã�ֱ��calloc�ֳ���
					p_object->p_pixbuf = (uint8 *) calloc(1, width * height);
					p_object->pos_bitMap = -1;
					if (NULL == p_object->p_pixbuf) {
						LOGE(
								"[DvbSubtitleMgr_parseObjectSegment]calloc p_pixbuf memory failed !!!!!!!!!\n");
						return 0;
					}
				} else {
					flag = DvbSubtitleMgr_getFreeBitMapBuffer(me);
					if (flag != -1) { //�ҵ����е�buffer�ˣ�
						p_object->pos_bitMap = flag;
						me->flag_buf_used[flag] = 1;
					} else { //�Σ�32�����������ˣ��Ǿ�ֱ��calloc
						p_object->p_pixbuf = (uint8 *) calloc(1,
								width * height);
						p_object->pos_bitMap = -1;
						if (NULL == p_object->p_pixbuf) {
							LOGE(
									"[DvbSubtitleMgr_parseObjectSegment]calloc p_pixbuf memory failed !!!!!!!!!\n");
							return 0;
						}
					}
				}
				/*�����泡��λͼ��Ϣ*/
				LOGI(
						"[DvbSubtitleMgr_parseObjectSegment]width = %d,height=%d,pos_bitmap = %d\n",
						p_object->width, p_object->height,
						p_object->pos_bitMap);

				SUBT_Decode_Object_Pixel_Data(me, p_object, data_ptr,
						topfield_len, width, 0);
				data_ptr += topfield_len;
				if (bottomfield_len) {
					/*����ż����λͼ��Ϣ*/
					SUBT_Decode_Object_Pixel_Data(me, p_object, data_ptr,
							bottomfield_len, width, 1);
				} else {
					/*���ż�������ݲ����ڣ����泡���ݵ���ż�����ݽ��н���*/
					SUBT_Decode_Object_Pixel_Data(me, p_object, data_ptr,
							topfield_len, width, 1);
				}
			}
		}
	} else {
		LOGE(
				"[DvbSubtitleMgr_parseObjectSegment]this object not in any regions object_id = %d!!!!!!!!!!!! \n",
				object_id);
	}
	return 1;
}

static void SUBT_YUV2RGB(uint8 *pRGB, uint8 *pYUV) {
	uint8 y, u, v;

	y = *pYUV;
	pYUV++;
	u = *pYUV;
	pYUV++;
	v = *pYUV;
	pYUV++;
	*pRGB = SUBT_YUV_TO_R(y, u, v);
	pRGB++;
	*pRGB = SUBT_YUV_TO_G(y, u, v);
	pRGB++;
	*pRGB = SUBT_YUV_TO_B(y, u, v);
}

/************************************************************************/
/** \brief ��һ��bg_code��ɫת��Ϊrgb8888��ɫ��p_clut��Ӧ��Ϊ��Ӧ����ɫ��
 * \reutrn : ��Ӧ��rgb8888��ɫֵ
 */
/************************************************************************/
int DvbSubtitleMgr_getBgColor(int bg_code, SubTitleCLUT *p_clut, uint8 depth) {
	int palData = 0x00;
	uint8 bufRGB[3] = { 0 };  // ��ʱ����{R,G,B}
	uint8 bufYUV[3] = { 0 };  // ��ʱ����{Y,U,V}
	uint32 *palette = NULL;
	uint32 result = 0;
	uint8 trans = 0;

	if (depth == SUBT_2BIT_ENTRY_CLUT) {
		palette = p_clut->clut_2b;
	} else if (depth == SUBT_4BIT_ENTRY_CLUT) {
		palette = p_clut->clut_4b;
	} else if (depth == SUBT_8BIT_ENTRY_CLUT) {
		palette = p_clut->clut_8b;
	}
	palData = palette[bg_code];
	trans = (uint8) (palData & SUBT_COLOR_T_MAKS);
	if (trans == 0) {  //ȫ͸��
		result = 0x00000000;
	} else {
		bufYUV[0] = (uint8) (palData & SUBT_COLOR_Y_MASK);
		bufYUV[1] = (uint8) ((palData & SUBT_COLOR_CB_MASK) >> 8);
		bufYUV[2] = (uint8) ((palData & SUBT_COLOR_CR_MASK) >> 16);
		SUBT_YUV2RGB(bufRGB, bufYUV);
		result = (bufRGB[2] << 24) | (bufRGB[1] << 16) | (bufRGB[0] << 8)
				| ((palData & SUBT_COLOR_T_MAKS) >> 24);
	}
	return result;
}

int DvbSubtitleMgr_getNumShowdatas(int h) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_getNumShowdatas] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	return me->numShowDatas;
}

SUBTShowData **DvbSubtitleMgr_getShowDataList(int h) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_getShowDataList] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	return me->listShowData;
}

/************************************************************************/
/** \brief ���ｫshowData ����Ҫ������Ľ������
 */
/************************************************************************/
void DvbSubtitleMgr_resetShowDataList(int h) {
	int i = 0;
	SUBTShowData *showData = NULL;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_resetShowDataList] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	for (i = 0; i < me->numShowDatas; i++) {
		showData = me->listShowData[i];
		if (showData->status == 4 || showData->status == 5) {  //��Ҫ�������
			if (showData->listObject) {
				int k = 0;
				SubtitleObject *p_object = NULL;
				for (k = 0; k < showData->numObjects; k++) {
					p_object = &showData->listObject[k];
					if (p_object->object_type == SUBT_OBJECT_BITMAP) {  //bitmap
						if (p_object->pos_bitMap == -1) {
							/*����ͼƬ�����ֳ������ڴ棬ֱ��free*/
							if (p_object->p_pixbuf) {
								free(p_object->p_pixbuf);
								p_object->p_pixbuf = NULL;
							}
						} else {
							/*����ʹ��Ԥ�ȷֳ������ڴ棬reset���ڴ�*/
							me->flag_buf_used[p_object->pos_bitMap] = 0;
							memset(
									(me->bitmap_buf
											+ p_object->pos_bitMap
													* BITMAP_BUFFER * 1024), 0,
									BITMAP_BUFFER * 1024);
						}
					} else {
						if (p_object->character_code) {
							free(p_object->character_code);
							p_object->character_code = NULL;
						}
					}
				}
				free(showData->listObject);
				showData->listObject = NULL;
			}
			free(showData);
			if (i < (me->numShowDatas - 1)) {
				memmove(&me->listShowData[i], &me->listShowData[i + 1],
						(me->numShowDatas - i - 1) * sizeof(SUBTShowData*));
				i--;
			}
			me->listShowData[me->numShowDatas - 1] = NULL;
			me->numShowDatas--;
		}
	}
}

uint8 *DvbSubtitleMgr_getBitMapBuffer(int h, int bitMap_pos) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_getBitMapBuffer] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	return (me->bitmap_buf + BITMAP_BUFFER * bitMap_pos * 1024);
}

SubTitleCLUT *DvbSubtitleMgr_getCLUT(int h, uint16 page_id, uint16 clut_id) {
	int i = 0;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_getCLUT] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	SubTitleCLUT *p_clut = NULL;
	for (i = 0; i < me->numCLUTs; i++) {
		p_clut = me->listCLUT[i];
		if (p_clut->page_id == page_id && p_clut->clut_id == clut_id) {
			return p_clut;
		}
	}
	return NULL;
}

/************************************************************************/
/** \brief ��������������Ӧ����Ҫ��ʾ��region���뵽showData�У��Ⱥ���ʾ
 */
/************************************************************************/
int DvbSubtitleMgr_addShowData(int h) {
	int i = 0;
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_addShowData] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	SubtitlePage *p_page = me->p_page;
	SubtitleRegion *p_region = NULL;
	SubTitleCLUT *p_clut = NULL;
	SubtitleObject *p_object = NULL;
	SubtitleObject *p_object1 = NULL;
	SUBTShowData *showData = NULL;

	LOGI("[DvbSubtitleMgr_addShowData]start !!!!!!!!!\n");

	if (!p_page) {
		return 0;
	}

	/************************************************************************/
	/*û����Ҫ��ʾ��regions,���״̬����NORMAL_CASE,��Ҫ֪ͨ�ڸ�ptsʱ���������Ļ������*/
	/*��Ҫ��������? ��numRegions = 0��ʱ���������Ļ���ǲ��������ʱ�����...*/
	/************************************************************************/
	if (p_page->numRegions == 0
			&& (p_page->page_state != SUBT_PAGE_NORMAL_CASE)) {
//		dvb2subtitle_proc(SUBTITLE_MESSAGE_CLEAR_DATA, p_page->page_time_out,
//				0);
		return 1;
	}

	/************************************************************************/
	/*ѭ�����Ҹ�display set��Ҫ��ʾ��region,Ҳ����p_page�����������е�region*/
	/************************************************************************/
	for (i = 0; i < p_page->numRegions; i++) {
		int j = 0;
		/************************************************************************/
		/*�����е�region�в�����Ҫ��ʾ��region*/
		/************************************************************************/
		for (j = 0; j < me->numRegions; j++) {
			p_region = me->listRegion[j];
			if (p_region->region_id == p_page->region_defs[i].region_id) {
				p_region->region_left = p_page->region_defs[i].left;
				p_region->region_top = p_page->region_defs[i].top;
				p_region->pts = p_page->region_defs[i].pts;
				break;
			}
		}
		if (j == me->numRegions)
			continue;

		/************************************************************************/
		/*������Ҫ ��ʾ��region��Ӧ��CLUT*/
		/************************************************************************/
		for (j = 0; j < me->numCLUTs; j++) {
			p_clut = me->listCLUT[j];
			if (p_clut->clut_id == p_region->clut_id) {
				break;
			}
		}
		/************************************************************************/
		/*���һ��ʼ�������normal case��״̬����ô�յ���region������û��CLUT��,Ŀǰ��ʱ����ɲ����뵽showdata��*/
		/*�����Ҫ��ʾ������ҪĬ��һ�� CLUT */
		/*�������Ӧ����Ҫ��ʾ��region��ռ�õ�object�Ŀռ�*/
		/************************************************************************/
		if (j == me->numCLUTs) {
			int k = 0;
			for (k = 0; k < p_region->numObjects; k++) {
				p_object = &p_region->listObject[k];
				if ((SUBT_OBJECT_BITMAP == p_object->object_type)) {
					if (p_object->pos_bitMap == -1) {
						if (p_object->p_pixbuf)
							free(p_object->p_pixbuf);
						p_object->p_pixbuf = NULL;
					} else {
						me->flag_buf_used[p_object->pos_bitMap] = 0;
						memset(
								(me->bitmap_buf
										+ p_object->pos_bitMap * BITMAP_BUFFER
												* 1024), 0,
								BITMAP_BUFFER * 1024);
					}
				}
			}
			continue;
		}

		/************************************************************************/
		/* ������NORMAL_CASE��״̬�£������Ƿ��region�Ѿ����뵽showData��; */
		/* ���������ͬ��region,ͬʱ�Ƚ�region������object������ͬ����Ϊ����ͬ��*/
		/************************************************************************/
		if (p_page->page_state == SUBT_PAGE_NORMAL_CASE) {
			for (j = 0; j < me->numShowDatas; j++) {
				showData = me->listShowData[j];
				if (showData->region_id == p_region->region_id
						&& showData->region_version
								== p_region->region_version) {
					//region��ͬ���ж�object�Ƿ���ͬ
					if (showData->numObjects == p_region->numObjects) {
						int k = 0;
						for (k = 0; k < showData->numObjects; k++) {
							p_object = &p_region->listObject[k];
							p_object1 = &showData->listObject[k];
							if (p_object->object_id == p_object1->object_id
									&& p_object->object_version
											== p_object1->object_version)
								break;
						}
						if (k != showData->numObjects) {
							break;
						}
					}
				}
			}
			if (j == me->numShowDatas) {  //new showData
				//��ȡһ��showData����
				showData = DvbSubtitleMgr_obtainShowData(me);
				if (NULL == showData) {
					//��ʾ����
					LOGE(
							"[DvbSubtitleMgr_addShowData]calloc showData memory failed !!!!!!!!!\n");
					return 0;
				}
			} else {				//�ҵ���ͬ��showData
				LOGE("[DvbSubtitleMgr_addShowData]same region!!\n");
				/************************************************************************/
				/* �����ҵ���ͬ����ʾregion��,���һ�λ�ͼ��pts���ܸ��ģ�ֻ�ܸ�����page_time_outʱ��*/
				/* �����Ƿ���Ҫ��ô��? ����������ȥ��*/
				/************************************************************************/
				//showData->pts = p_region->pts;
				if (p_region->pts > showData->pts) {
					showData->page_time_out += (p_region->pts - showData->pts);
				}
				continue;
			}
		} else {
			showData = DvbSubtitleMgr_obtainShowData(me);
			if (NULL == showData) {
				//��ʾ����
				LOGE(
						"[DvbSubtitleMgr_addShowData]calloc showData memory failed !!!!!!!!!\n");
				return 0;
			}
		}

		showData->state = me->p_page->page_state;
		showData->height = p_region->region_height;
		showData->width = p_region->region_width;
		showData->page_id = me->p_page->page_id;
		showData->region_id = p_region->region_id;
		showData->left = p_region->region_left;
		showData->top = p_region->region_top;
		showData->fill_flag = p_region->fill_flag;
		showData->page_time_out = me->p_page->page_time_out;
		showData->page_version = me->p_page->page_version;

#if defined(WIN32)
		showData->pts = 0; //ģ����������,����Ϊ0
#else
		showData->pts = p_region->pts;
#endif

		showData->region_version = p_region->region_version;
		showData->numObjects = 0;
		showData->status = 1;
		//������Ҫ�����е���ɫ����ӳ��.
		if (showData->fill_flag) { //��Ҫ�������,������ת��ΪRGB ��ʽ
			showData->background_code = DvbSubtitleMgr_getBgColor(
					p_region->background_code, p_clut, p_region->region_depth);
		}

		showData->numObjects = p_region->numObjects;
		showData->clut_id = p_region->clut_id;
		showData->depth = p_region->region_depth;
		if (showData->numObjects > 0) {
			showData->listObject = (SubtitleObject *) calloc(1,
					sizeof(SubtitleObject) * showData->numObjects);
			if (NULL == showData->listObject) {
				showData->numObjects = 0;
				LOGE(
						"[DvbSubtitleMgr_addShowData]calloc showData objects memory failed !!!!!!!!!\n");
			}
			for (j = 0; j < p_region->numObjects; j++) {
				p_object = &p_region->listObject[j];
				p_object1 = &showData->listObject[j];
				p_object1->object_id = p_object->object_id;
				p_object1->object_version = p_object->object_version;
				p_object1->left = p_object->left + p_region->region_left;
				p_object1->top = p_object->top + p_region->region_top;
				p_object1->object_type = p_object->object_type;

				if ((SUBT_OBJECT_CHARACTER == p_object->object_type)
						|| (SUBT_OBJECT_STRING == p_object->object_type)) {
					p_object1->character_code = (uint8 *) calloc(1,
							sizeof(p_object->character_code));
					if (NULL == p_object1->character_code) {
						//��ʾ����
						LOGE(
								"[DvbSubtitleMgr_addShowData]calloc showData object character_code memory failed !!!!!!!!!\n");
						break;
					}
					strcpy((char *) p_object1->character_code,
							(char *) p_object->character_code);
					p_object1->background = DvbSubtitleMgr_getBgColor(
							p_object->background, p_clut,
							p_region->region_depth);
					p_object1->foreground = DvbSubtitleMgr_getBgColor(
							p_object->foreground, p_clut,
							p_region->region_depth);
				} else { //bitMap
					p_object1->width = p_object->width;
					p_object1->height = p_object->height;
					if (p_object->pos_bitMap == -1) {
						p_object1->p_pixbuf = p_object->p_pixbuf; //showData ��objectֱ�����ߣ���ʾ��Ϻ��ͷ�.
						p_object->p_pixbuf = NULL;
						p_object1->pos_bitMap = -1;
					} else {
						p_object1->pos_bitMap = p_object->pos_bitMap;
					}
				}
			}
		}
	}
	LOGI("[DvbSubtitleMgr_addShowData]end!!!!!!! number_showDatas = %d\n",
			me->numShowDatas);

	return 1;
}

/************************************************************************/
/** \brief ����subtitle����
 * \reutrn : ��ʵ������
 */
/************************************************************************/
static
int DvbSubtitleMgr_parseSubtitle(Subtitle_t *me, uint8 *buf, int len,
		uint32 pts) {
	uint8 sync_byte;
	uint8 segment_type;
	uint16 page_id;
	uint16 segment_length;
	uint8 *data_ptr = buf + 2;
	uint8 *end_ptr = buf + len - 1;
	int end_parse = 0;
	int ProcessedBytes = 0;
	uint8 data_indetify = buf[0];
	uint8 stream_id = buf[1];
	if (data_indetify != 0x20 || stream_id != 0x00) {
		LOGE(
				"[DvbSubtitleMgr_parseSubtitle]wrong data_indetify = %d ,stream_id = %d !!!!!!!!!!!\n",
				data_indetify, stream_id);
		return 0;
	}
	while (data_ptr < end_ptr) {
		if ((data_ptr + SUBT_SEG_HEADER_LENGTH) > end_ptr) {
			LOGE(
					"[DvbSubtitleMgr_parseSubtitle] Segment is too short !!!!!!!!!\n");
			return 0;
		}
		segment_length = (data_ptr[4] << 8) | data_ptr[5];
		if ((segment_length + SUBT_SEG_HEADER_LENGTH) > SEGMENT_MAX_LENGTH) {
			LOGE(
					"[DvbSubtitleMgr_parseSubtitle] Segment is too long !!!!!!!!!\n");
		}
		sync_byte = data_ptr[0];
		if (sync_byte != SUBT_SUBTITLE_SYNC_BYTE) {
			LOGE(
					"[DvbSubtitleMgr_parseSubtitle]wrong sync_byte = %d !!!!!!!!!!!!\n",
					sync_byte);
			data_ptr += (SUBT_SEG_HEADER_LENGTH + segment_length);
			continue;
		}
		segment_type = data_ptr[1];
		page_id = (data_ptr[2] << 8) | data_ptr[3];

		if (me->com_page_id != page_id && me->anc_page_id != 0xffff
				&& me->anc_page_id != page_id && me->com_page_id != 0xffff) {
			//����ֱ�ӷ��ػ᲻��������?
			LOGE("[DvbSubtitleMgr_parseSubtitle]wrong page_id = %d\n", page_id);
			data_ptr += (SUBT_SEG_HEADER_LENGTH + segment_length);
			continue;
		}
		data_ptr += SUBT_SEG_HEADER_LENGTH;
		switch (segment_type) {
		case SUBT_PAGE_COMPOSITION_SEGMENT:
			DvbSubtitleMgr_parsePageSegment(me, data_ptr, segment_length, pts,
					page_id);
			data_ptr += segment_length;
			break;

		case SUBT_REGION_COMPOSITION_SEGMENT:
			DvbSubtitleMgr_parseRegionSegment(me, data_ptr, segment_length,
					page_id);
			data_ptr += segment_length;
			break;

		case SUBT_CLUT_DEFINITION_SEGMENT:
			DvbSubtitleMgr_parseCLUTSegment(me, data_ptr, segment_length,
					page_id);
			data_ptr += segment_length;
			break;

		case SUBT_OBJECT_DATA_SEGMENT:
			DvbSubtitleMgr_parseObjectSegment(me, data_ptr, segment_length,
					page_id);
			data_ptr += segment_length;
			break;

		case SUBT_EOD_SET_SEGMENT:
			data_ptr += segment_length;
			end_parse = 1;
			break;

		case SUBT_STUFFING_SEGMENT:
			data_ptr += segment_length;
			break;

		default:
			//��ʾ
			LOGE("[DvbSubtitleMgr_parseSubtitle]segment_type = 0x%x\n",
					segment_type);
			data_ptr += segment_length;
			break;
		}
	}
	//�������
	if (end_parse) {
		DvbSubtitleMgr_addShowData(me);
	}
	LOGI("[DvbSubtitleMgr_parsePES_end] timer = %d\n", time_ms());

	return 1;
}

/************************************************************************/
/** \brief ����PES��������,��ȡ����Ӧ��PTS����Ļ����Seg
 * \reutrn : ��ʵ������
 */
/************************************************************************/
static
int DvbSubtitleMgr_parsePES1(int h, uint8 *buf, int len) {
	DvbSubtitleMgr *handle = (DvbSubtitleMgr *) SubtitleHandle;

	if (NULL == handle || h <= 0 || h > MAX_SUBTITLE_NUM) {
		LOGE("h = %s [DvbSubtitleMgr_parsePES1] failed, param invalid\n", h);
		return 0;
	}
	Subtitle_t *me = &handle->subtitle[h];
	int packet_length = 0;
	uint8 *ptr = buf + 3;
	uint8 stream_id = *ptr++;
	uint8 PTS_DTS_flag = 0;
	int head_length = 0;
	int subt_seg_len = 0;
	uint8 *subt_buffer;
	uint32 pts = 0;
	uint32 tmp_PTS1 = 0;
	uint32 tmp_PTS2 = 0;
	uint32 tmp_PTS3 = 0;

	if (stream_id != 0xBD) {
		LOGE("[DvbSubtitleMgr_parsePES]wrong stream_id = %d , len =%d\n",
				stream_id, len);
		return 0;
	}
	GET16(packet_length, ptr);
	ptr++;
	PTS_DTS_flag = ((ptr[0] & 0xC0) >> 6);
	ptr++;
	head_length = *ptr++;
	subt_seg_len = (packet_length - head_length - 3);
	subt_buffer = buf + 9 + head_length;
	if (PTS_DTS_flag != 0x02 && PTS_DTS_flag != 0x03) {
		LOGE("[DvbSubtitleMgr_parsePES]wrong PTS_DTS_flag = %d\n",
				PTS_DTS_flag);
		return 0;
	}

	/*��ȡ��PTS��ȡ��32λ*/
	tmp_PTS1 |= (uint32) (ptr[0] & 0x0f);
	tmp_PTS1 = (tmp_PTS1 << 28) & 0xe0000000;

	tmp_PTS2 = (ptr[1] << 8) | ptr[2];
	tmp_PTS2 = (tmp_PTS2 >> 1) & 0x00007fff;
	tmp_PTS2 = (tmp_PTS2 << 14) & 0x3FFFC000;

	tmp_PTS3 = (ptr[3] << 8) | ptr[4];
	tmp_PTS3 = (tmp_PTS3 >> 2) & 0x00003fff;

	pts = (tmp_PTS1 | tmp_PTS2 | tmp_PTS3) / 45;

	LOGI("[DvbSubtitleMgr_parsePES_start] timer = %d , pts =%d \n", time_ms(),
			pts);

	DvbSubtitleMgr_parseSubtitle(me, subt_buffer, subt_seg_len, pts);
	return 1;
}
