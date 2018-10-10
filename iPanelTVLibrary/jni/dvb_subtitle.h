#ifndef CA_MODULE_INTERFACE__H_
#define CA_MODULE_INTERFACE__H_

#include "stdio.h"
#include <pthread.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef unsigned char uint8;
typedef unsigned char uchar;
typedef unsigned long uint32;
typedef unsigned short uint16;
typedef short int16;
typedef int int32;

#define SUBTITLE_TIME_OUT									30000
#define SEGMENT_MAX_LENGTH									(64*1024)  /*max pes length */
#define PES_BUFFER											256		/*pes_buf 以K为单位，用来缓存PES包数据*/
#define	BITMAP_BUFFER										32		/*每个最大32K,用来缓存bitMpa图片*/
#define MAX_SAVE_BITMAP_NUM									32		/*最多缓存32个bitmap*/
#define	MAX_PES_NUMBER										256		/*最大可以缓存256个PES,够多了....*/

#define SUBT_SEG_HEADER_LENGTH								0x06   /*seg header length */
#define SUBT_SUBTITLE_SYNC_BYTE								0x0F   /* sync_byte */

/* subtitle segment type */
#define SUBT_PAGE_COMPOSITION_SEGMENT						0x10  /* page segment */
#define SUBT_REGION_COMPOSITION_SEGMENT						0x11  /* region segment */
#define SUBT_CLUT_DEFINITION_SEGMENT						0x12  /* CLUT segment */
#define SUBT_OBJECT_DATA_SEGMENT							0x13  /* Object segment*/
#define SUBT_EOD_SET_SEGMENT								0x80  /* EOD segment */
#define SUBT_STUFFING_SEGMENT								0xff  /* stuffing */

/* subtitle page state */
#define SUBT_PAGE_NORMAL_CASE								0x00  /* page update */
#define SUBT_PAGE_ACQUISITION_POINT							0x01  /* page refresh */
#define SUBT_PAGE_MODE_CHANGE								0x02  /* new page    */
#define SUBT_PAGE_RESERVED									0x03  /* reserved   */

/* subtitle object type */
#define SUBT_OBJECT_BITMAP									0x00  /* bitmap */
#define SUBT_OBJECT_CHARACTER								0x01  /* character */
#define SUBT_OBJECT_STRING									0x02  /* string */
#define SUBT_OBJECT_H264									0x03  /* reserved(for H.264 protocol) */

/* subtitle object coding method */
#define SUBT_OBJECT_CODING_PIXEL							0x00  /* coding of pixels */
#define SUBT_OBJECT_CODING_CHARACTER						0x01  /* coding of characters*/
#define SUBT_OBJECT_CODING_H264								0x03  /* coging of H.264 */

/* subtitle pixel block type */
#define SUBT_BLOCK_2BIT_CODE_STRING							0x10  /*2-bit/pixel code string */
#define SUBT_BLOCK_4BIT_CODE_STRING							0x11  /*4-bit/pixel code string */
#define SUBT_BLOCK_8BIT_CODE_STRING							0x12  /*8-bit/pixel code string */
#define SUBT_BLOCK_2TO4BIT_MAP_TABLE						0x20  /*2 to 4-bit map-table data */
#define SUBT_BLOCK_2TO8BIT_MAP_TABLE						0x21  /*2 to 8-bit map-table data */
#define SUBT_BLOCK_4TO8BIT_MAP_TABLE						0x22  /*4 to 8-bit map-table data */
#define SUBT_BLOCK_END_OF_OBJECT_LINE						0xF0  /* end of object line code */

/* subtitle CLUT compatibility */
#define SUBT_2BIT_ENTRY_CLUT								0x01  /* 2-bit/entry CLUT requires */
#define SUBT_4BIT_ENTRY_CLUT								0x02  /* 4-bit/entry CLUT requires */
#define SUBT_8BIT_ENTRY_CLUT								0x03  /* 8-bit/entry CLUT requires */

#define SUBT_COLOR_Y_MASK									(0x000000FF)
#define SUBT_COLOR_CB_MASK									(0x0000FF00)
#define SUBT_COLOR_CR_MASK									(0x00FF0000)
#define SUBT_COLOR_T_MAKS									(0xFF000000)

#define MAX_SUBTITLE_NUM	(3)

#define SUBT_BOUND(x,top,bot)   ( (x > top) ? top : ( (x < bot) ? bot : x) )
#define SUBT_YUV_TO_R(y,cr,cb)  (SUBT_BOUND(((1192*(y-16)+1608*(cr-128))>>10),255,0))
#define SUBT_YUV_TO_G(y,cr,cb)  (SUBT_BOUND(((1192*(y-16)-832*(cr-128)-401*(cb-128))>>10),255,0))
#define SUBT_YUV_TO_B(y,cr,cb)  (SUBT_BOUND(((1192*(y-16)+2066*(cb-128))>>10),255,0))

#define GET16(a,ptr) (a=ptr[0]<<8 | ptr[1])

#define	SUB_STATE_INITED  									0x01
#define	SUB_STATE_STARTED									0x02
#define	SUB_STATE_STOPED									0x03

#ifdef __cplusplus
}
;
#endif

#endif /*  CA_MODULE_INTERFACE__H_*/

