package com.ipanel.chongqing_ipanelforhw.hwstruct;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.ipanel.chongqing_ipanelforhw.R.array;


public class GetHwResponse implements Serializable {

	

	public class Error implements Serializable {

		@Expose
		private int code;

		@Expose
		private String info;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getInfo() {
			return info;
		}

		public void setInfo(String info) {
			this.info = info;
		}

	}

	@Expose
	private Error error;
	
	public Error getError() {
		return error;
	}



	public void setError(Error error) {
		this.error = error;
	}
	
	@Expose
	private List<String> search_keyword;
	/**
	 * 订阅的标签
	 */
	@Expose
	private List<String> tags;
	
	
	public List<String> getTags() {
		return tags;
	}



	public void setTags(List<String> tags) {
		this.tags = tags;
	}



	public List<String> getSearch_keyword() {
		return search_keyword;
	}



	public void setSearch_keyword(List<String> search_keyword) {
		this.search_keyword = search_keyword;
	}

	@Expose
	private int total;
	
	@Expose
	private int subscribe_tag;
	/**
	 * 明星是否关注：1为已关注；0为未关注
	 */
	@Expose
	private int isFollowed;
	
	public int getIsFollowed() {
		return isFollowed;
	}

	public void setIsFollowed(int isFollowed) {
		this.isFollowed = isFollowed;
	}

	@Expose
	private int num;
	
	@Expose
	private List<Program> programs;
	
	@Expose
	private Program program;
	
	
	
	public Program getProgram() {
		return program;
	}



	public void setProgram(Program program) {
		this.program = program;
	}

	

	public Wiki getWiki() {
		return wiki;
	}



	public void setWiki(Wiki wiki) {
		this.wiki = wiki;
	}



	public List<Program> getPrograms() {
		return programs;
	}



	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}

	public int getTotal() {
		return total;
	}

	public int getSubscribe_tag() {
		return subscribe_tag;
	}

	public void setSubscribe_tag(int subscribe_tag) {
		this.subscribe_tag = subscribe_tag;
	}



	public void setTotal(int total) {
		this.total = total;
	}



	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	@Expose
	private List<Channel> channels;
	
	public List<Channel> getChannels() {
		return channels;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}
	
	public class Channel implements Serializable {
		
		@Expose
		private String channelId;
		
		@Expose
		private String name;
		
		@Expose
		private int number;
		
		@Expose
		private String serviceId;
		
		@Expose
		private String frequency;
		
		@Expose
		private List<String> type;
		
		@Expose
		private String logo;
		
		
		@Expose
		private String curName;
		
		@Expose
		private List<String> curType;
		
		@Expose
		private String startTime;
		
		
		@Expose
		private String endTime;
		
		
		@Expose
		private String nextName;
		
		@Expose
		private String wikiId;
		
		@Expose
		private String wikiTitle;
		
		@Expose
		private String wikiCover;

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getFrequency() {
			return frequency;
		}

		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}

		public List<String> getType() {
			return type;
		}

		public void setType(List<String> type) {
			this.type = type;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}

		public String getCurName() {
			return curName;
		}

		public void setCurName(String curName) {
			this.curName = curName;
		}

		public List<String> getCurType() {
			return curType;
		}

		public void setCurType(List<String> curType) {
			this.curType = curType;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		public String getNextName() {
			return nextName;
		}

		public void setNextName(String nextName) {
			this.nextName = nextName;
		}

		public String getWikiId() {
			return wikiId;
		}

		public void setWikiId(String wikiId) {
			this.wikiId = wikiId;
		}

		public String getWikiTitle() {
			return wikiTitle;
		}

		public void setWikiTitle(String wikiTitle) {
			this.wikiTitle = wikiTitle;
		}

		public String getWikiCover() {
			return wikiCover;
		}

		public void setWikiCover(String wikiCover) {
			this.wikiCover = wikiCover;
		}
		
		
	}

	public class Program implements Serializable {
		
		@Expose
		private String name;
		
		@Expose
		private String date;
		
		@Expose
		private String title;
		
		@Expose
		private String startTime;
		
		@Expose
		private String endTime;
		
		@Expose
		private String start_time;
		
		@Expose
		private String end_time;
		
		
		@Expose
		private String nextName;
		
		@Expose
		private List<String> type;
		
	
		
		@Expose
		private String channelId;
		
		@Expose
		private String channelName;
		
		@Expose
		private int channelNumber;
		
		@Expose
		private String channelLogo;
		
		@Expose
		private String serviceId;
		
		@Expose
		private String frequency;
		
	
		
	
		
		@Expose
		private String curName;
		
		@Expose
		private List<String> curType;
		
		
		
		
		
		
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}

		public void setChannelNumber(int channelNumber) {
			this.channelNumber = channelNumber;
		}

		@Expose
		private String wikiId;
		
		@Expose
		private String wikiTitle;
		
		@Expose
		private String wiki_id;
		
		@Expose
		private String wiki_title;
		
		@Expose
		private String cover;
		
		
		@Expose
		private String model;
		
		@Expose
		private List<String> posters;
		
		@Expose
		private List<String> tags;
		
		
		
		

		public List<String> getPosters() {
			return posters;
		}

		public void setPosters(List<String> posters) {
			this.posters = posters;
		}

		public String getCurName() {
			return curName;
		}

		public void setCurName(String curName) {
			this.curName = curName;
		}

		public List<String> getCurType() {
			return curType;
		}

		public void setCurType(List<String> curType) {
			this.curType = curType;
		}

		public String getStartTime() {
			return startTime;
		}

		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}

		public String getEndTime() {
			return endTime;
		}

		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}

		public String getNextName() {
			return nextName;
		}

		public void setNextName(String nextName) {
			this.nextName = nextName;
		}

		public String getWikiId() {
			return wikiId;
		}

		public void setWikiId(String wikiId) {
			this.wikiId = wikiId;
		}

		public String getWikiTitle() {
			return wikiTitle;
		}

		public void setWikiTitle(String wikiTitle) {
			this.wikiTitle = wikiTitle;
		}

		public String getCover() {
			return cover;
		}

		public void setCover(String cover) {
			this.cover = cover;
		}

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}

		public String getChannelName() {
			return channelName;
		}

		public void setName(String channelName) {
			this.channelName = channelName;
		}

		public int getChannelNumber() {
			return channelNumber;
		}

		public void setNumber(int channelNumber) {
			this.channelNumber = channelNumber;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getFrequency() {
			return frequency;
		}

		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}

		public List<String> getType() {
			return type;
		}

		public void setType(List<String> type) {
			this.type = type;
		}

		public String getChannelLogo() {
			return channelLogo;
		}

		public void setChannelLogo(String channelLogo) {
			this.channelLogo = channelLogo;
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getStart_time() {
			return start_time;
		}

		public void setStart_time(String start_time) {
			this.start_time = start_time;
		}

		public String getEnd_time() {
			return end_time;
		}

		public void setEnd_time(String end_time) {
			this.end_time = end_time;
		}

		public String getWiki_id() {
			return wiki_id;
		}

		public void setWiki_id(String wiki_id) {
			this.wiki_id = wiki_id;
		}

		public String getWiki_title() {
			return wiki_title;
		}

		public void setWiki_title(String wiki_title) {
			this.wiki_title = wiki_title;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public String getName() {
			return name;
		}
		
		
		
	}
	
	
	
	@Expose
	private List<Wiki> wikis;
	
	@Expose
	private Wiki wiki;
	/**
	 * 获取演员详情：相关影片
	 */
	@Expose
	private List<Wiki> videos;
	/**
	 * 获取演员详情：相关资讯
	 */
	@Expose
	private List<Wiki> televisions;
	
	public class Wiki implements Serializable {
		
		@Expose
		private String wikiId;
		
		@Expose
		private String wikiTitle;
		
		
		
		
		public String getWikiId() {
			return wikiId;
		}

		public void setWikiId(String wikiId) {
			this.wikiId = wikiId;
		}

		public String getWikiTitle() {
			return wikiTitle;
		}

		public void setWikiTitle(String wikiTitle) {
			this.wikiTitle = wikiTitle;
		}

		public Starrings getStarrings() {
			return starrings;
		}

		public void setStarrings(Starrings starrings) {
			this.starrings = starrings;
		}

		@Expose
		private String id;
		
		@Expose
		private String title;
		

		
		@Expose
		private String desc;
		
		
		@Expose
		private List<String> tags;
		
		@Expose
		private String model;
		
		@Expose
		private String cover;
		
		@Expose
		private List<String> posters;
		
		@Expose
		private Info info;
		
		public class Info implements Serializable {
			
			@Expose
			private List<String> alias;
			
			@Expose
			private List<String> host;
			
			@Expose
			private List<String> guest;
			
			@Expose
			private String channel;
			
			@Expose
			private String play_time;
			
			@Expose
			private List<Star> producer;
			
			@Expose
			private String runtime;
			
			@Expose
			private List<Star> director;
			
			@Expose
			private List<Star> starring;
			
			@Expose
			private String released;
			
			@Expose
			private String language;
			
			@Expose
			private String country;
			
			@Expose
			private List<String> writer;
			
			@Expose
			private List<String> distributor;
			
			@Expose
			private String episodes;
			
			@Expose
			private String produced;
			
			@Expose
			private String average;
			
			@Expose
			private int is_fav;
			/**
			 * 收藏数
			 */
			@Expose
			private int fav_num;
			/**
			 * 点赞数
			 */
			@Expose
			private int inter_up_num;
			/**
			 * 点衰数
			 */
			@Expose
			private int inter_down_num;
			@Expose
			private int is_inter;
			@Expose
			private int vod_num;
			
			public class Star implements Serializable{
				public String getId() {
					return id;
				}

				public void setId(String id) {
					this.id = id;
				}

				public String getTitle() {
					return title;
				}

				public void setTitle(String title) {
					this.title = title;
				}

				public String getAvatar() {
					return avatar;
				}

				public void setAvatar(String avatar) {
					this.avatar = avatar;
				}

				@Expose
				private String id;
				
				@Expose
				private String title;
				
				@Expose
				private String avatar;
			}

			public int getIs_fav() {
				return is_fav;
			}

			public void setIs_fav(int is_fav) {
				this.is_fav = is_fav;
			}

			public int getFav_num() {
				return fav_num;
			}

			public void setFav_num(int fav_num) {
				this.fav_num = fav_num;
			}

			public int getInter_up_num() {
				return inter_up_num;
			}

			public void setInter_up_num(int inter_up_num) {
				this.inter_up_num = inter_up_num;
			}

			public int getInter_down_num() {
				return inter_down_num;
			}

			public void setInter_down_num(int inter_down_num) {
				this.inter_down_num = inter_down_num;
			}

			public int getIs_inter() {
				return is_inter;
			}

			public void setIs_inter(int is_inter) {
				this.is_inter = is_inter;
			}

			public int getVod_num() {
				return vod_num;
			}

			public void setVod_num(int vod_num) {
				this.vod_num = vod_num;
			}

			public List<String> getHost() {
				return host;
			}

			public void setHost(List<String> host) {
				this.host = host;
			}

			public List<String> getGuest() {
				return guest;
			}

			public void setGuest(List<String> guest) {
				this.guest = guest;
			}

			public String getChannel() {
				return channel;
			}

			public void setChannel(String channel) {
				this.channel = channel;
			}

			public String getPlay_time() {
				return play_time;
			}

			public void setPlay_time(String play_time) {
				this.play_time = play_time;
			}

			public List<Star> getProducer() {
				return producer;
			}

			public void setProducer(List<Star> producer) {
				this.producer = producer;
			}

			public String getRuntime() {
				return runtime;
			}

			public void setRuntime(String runtime) {
				this.runtime = runtime;
			}

			public String getAverage() {
				return average;
			}

			public void setAverage(String average) {
				this.average = average;
			}

			public List<String> getAlias() {
				return alias;
			}

			public void setAlias(List<String> alias) {
				this.alias = alias;
			}

			public List<Star> getDirector() {
				return director;
			}

			public void setDirector(List<Star> director) {
				this.director = director;
			}

			public List<Star> getStarring() {
				return starring;
			}

			public void setStarring(List<Star> starring) {
				this.starring = starring;
			}

			public String getReleased() {
				return released;
			}

			public void setReleased(String released) {
				this.released = released;
			}

			public String getLanguage() {
				return language;
			}

			public void setLanguage(String language) {
				this.language = language;
			}

			public String getCountry() {
				return country;
			}

			public void setCountry(String country) {
				this.country = country;
			}

			public List<String> getWriter() {
				return writer;
			}

			public void setWriter(List<String> writer) {
				this.writer = writer;
			}

			public List<String> getDistributor() {
				return distributor;
			}

			public void setDistributor(List<String> distributor) {
				this.distributor = distributor;
			}

			public String getEpisodes() {
				return episodes;
			}

			public void setEpisodes(String episodes) {
				this.episodes = episodes;
			}

			public String getProduced() {
				return produced;
			}

			public void setProduced(String produced) {
				this.produced = produced;
			}
			
			
			/*
			 * actor
			 */

			
			@Expose
			private String english_name;
			
			@Expose
			private String nickname;
			
			
			@Expose
			private String sex;
			
			@Expose
			private String birthday;

			
			@Expose
			private String birthplace;
			
			@Expose
			private String occupation;
			
			@Expose
			private String nationality;
			
			@Expose
			private String zodiac;
			
			@Expose
			private String debut;
			
			@Expose
			private String bloodType;
			
			@Expose
			private String height;
			
			@Expose
			private String weight;
			
			@Expose
			private String region;
			
			@Expose
			private String aspect;

			public String getEnglish_name() {
				return english_name;
			}

			public void setEnglish_name(String english_name) {
				this.english_name = english_name;
			}

			public String getNickname() {
				return nickname;
			}

			public void setNickname(String nickname) {
				this.nickname = nickname;
			}

			public String getSex() {
				return sex;
			}

			public void setSex(String sex) {
				this.sex = sex;
			}

			public String getBirthday() {
				return birthday;
			}

			public void setBirthday(String birthday) {
				this.birthday = birthday;
			}

			public String getBirthplace() {
				return birthplace;
			}

			public void setBirthplace(String birthplace) {
				this.birthplace = birthplace;
			}

			public String getOccupation() {
				return occupation;
			}

			public void setOccupation(String occupation) {
				this.occupation = occupation;
			}

			public String getNationality() {
				return nationality;
			}

			public void setNationality(String nationality) {
				this.nationality = nationality;
			}

			public String getZodiac() {
				return zodiac;
			}

			public void setZodiac(String zodiac) {
				this.zodiac = zodiac;
			}

			public String getDebut() {
				return debut;
			}

			public void setDebut(String debut) {
				this.debut = debut;
			}

			public String getBloodType() {
				return bloodType;
			}

			public void setBloodType(String bloodType) {
				this.bloodType = bloodType;
			}

			public String getHeight() {
				return height;
			}

			public void setHeight(String height) {
				this.height = height;
			}

			public String getWeight() {
				return weight;
			}

			public void setWeight(String weight) {
				this.weight = weight;
			}

			public String getRegion() {
				return region;
			}

			public void setRegion(String region) {
				this.region = region;
			}

			public String getAspect() {
				return aspect;
			}

			public void setAspect(String aspect) {
				this.aspect = aspect;
			}
		}
		
		public List<String> getPosters() {
			return posters;
		}

		public void setPosters(List<String> posters) {
			this.posters = posters;
		}

		@Expose
		private String description;
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
		
		@Expose
		private List<String> screens;
		
		@Expose
		private String wikiCover;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public Info getInfo() {
			return info;
		}

		public void setInfo(Info info) {
			this.info = info;
		}

		public List<String> getScreens() {
			return screens;
		}

		public void setScreens(List<String> screens) {
			this.screens = screens;
		}

		public String getWikiCover() {
			return wikiCover;
		}

		public void setWikiCover(String wikiCover) {
			this.wikiCover = wikiCover;
		}
		
	
		@Expose
		private String alias;
		
		@Expose
		private String brithplace;
		
		@Expose
		private String douban_id;
		
		@Expose
		private String pinyin_title;
		
		@Expose
		private String english_name;
		
		@Expose
		private String sex;

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public String getBrithplace() {
			return brithplace;
		}

		public void setBrithplace(String brithplace) {
			this.brithplace = brithplace;
		}

		public String getDouban_id() {
			return douban_id;
		}

		public void setDouban_id(String douban_id) {
			this.douban_id = douban_id;
		}

		public String getPinyin_title() {
			return pinyin_title;
		}

		public void setPinyin_title(String pinyin_title) {
			this.pinyin_title = pinyin_title;
		}

		public String getEnglish_name() {
			return english_name;
		}

		public void setEnglish_name(String english_name) {
			this.english_name = english_name;
		}

		public String getSex() {
			return sex;
		}

		public void setSex(String sex) {
			this.sex = sex;
		}
		@Expose
		private String content;

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getCover() {
			return cover;
		}

		public void setCover(String cover) {
			this.cover = cover;
		}
		
		
		@Expose
		private Starrings starrings;
	
		public class Starrings implements Serializable {
			@Expose
			private String id;
			
			@Expose
			private String title;
			
			@Expose
			private String avatar;

			public String getId() {
				return id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String getTitle() {
				return title;
			}

			public void setTitle(String title) {
				this.title = title;
			}

			public String getAvatar() {
				return avatar;
			}

			public void setAvatar(String avatar) {
				this.avatar = avatar;
			}
			
			
		}
		
		
	}



	public List<Wiki> getWikis() {
		return wikis;
	}

	public void setWikis(List<Wiki> wikis) {
		this.wikis = wikis;
	}
	
	
	public List<Wiki> getVideos() {
		return videos;
	}



	public void setVideos(List<Wiki> videos) {
		this.videos = videos;
	}



	public List<Wiki> getTelevisions() {
		return televisions;
	}



	public void setTelevisions(List<Wiki> televisions) {
		this.televisions = televisions;
	}

	@Expose
	private List<Vod> vod;
	
	

	public List<Vod> getVod() {
		return vod;
	}

	public void setVod(List<Vod> vod) {
		this.vod = vod;
	}



	public class Vod implements Serializable {
		
		@Expose
		private String id;
		
		@Expose
		private String code;
		
		@Expose
		private String name;
		
		@Expose
		private String mark;
		
		@Expose
		private String parent_id;
			

		
		
		
		public String getParent_id() {
			return parent_id;
		}

		public void setParent_id(String parent_id) {
			this.parent_id = parent_id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMark() {
			return mark;
		}

		public void setMark(String mark) {
			this.mark = mark;
		}

		@Expose
		private String mediaId;

		@Expose
		private String title;

		@Expose
		private String pic;

		public String getMediaId() {
			return mediaId;
		}

		public void setMediaId(String mediaId) {
			this.mediaId = mediaId;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getPic() {
			return pic;
		}

		public void setPic(String pic) {
			this.pic = pic;
		}
		
		
	}
	
	@Expose
	private List<Filter> filters;
	
	public class Filter implements Serializable {
		
		public String getName() {
			return name;
		}



		public void setName(String name) {
			this.name = name;
		}



		public List<FilterItem> getFilter() {
			return filter;
		}



		public void setFilter(List<FilterItem> filter) {
			this.filter = filter;
		}



		@Expose
		private String name;

		@Expose
		private List<FilterItem> filter;

	
		
		public class FilterItem implements Serializable {
			@Expose
			private String name;
			
			@Expose
			private String value;

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getValue() {
				return value;
			}

			public void setValue(String value) {
				this.value = value;
			}
			
			
		
		}
	}
	
	@Expose
	private Qrcode Qrcode;

	public class Qrcode implements Serializable {
		@Expose
		private String url;
		
		@Expose
		private String expire;
		
		@Expose
		private String ticket;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getExpire() {
			return expire;
		}

		public void setExpire(String expire) {
			this.expire = expire;
		}

		public String getTicket() {
			return ticket;
		}

		public void setTicket(String ticket) {
			this.ticket = ticket;
		}
		
		
	}
	
	
	@Expose
	private List<User> Users;
			
	
	public class User implements Serializable {
		
		@Expose
		private String openid;
		
		@Expose
		private String nickname;
		
		@Expose
		private String headimgurl;
		
		@Expose
		private String backgroudurl;

		public String getOpenid() {
			return openid;
		}

		public void setOpenid(String openid) {
			this.openid = openid;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getHeadimgurl() {
			return headimgurl;
		}

		public void setHeadimgurl(String headimgurl) {
			this.headimgurl = headimgurl;
		}

		public String getBackgroudurl() {
			return backgroudurl;
		}

		public void setBackgroudurl(String backgroudurl) {
			this.backgroudurl = backgroudurl;
		}
		
		
		
	}



	public List<Filter> getFilters() {
		return filters;
	}



	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}



	public Qrcode getQrcode() {
		return Qrcode;
	}



	public void setQrcode(Qrcode qrcode) {
		Qrcode = qrcode;
	}



	public List<User> getUsers() {
		return Users;
	}



	public void setUsers(List<User> users) {
		Users = users;
	}
	
	

}
