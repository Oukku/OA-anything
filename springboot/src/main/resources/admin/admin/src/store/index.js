import Vue from 'vue'
import Vuex from 'vuex'
import http from '@/utils/http'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    menuList: []
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER(state, user) {
      state.userInfo = user
      localStorage.setItem('userInfo', JSON.stringify(user))
    },
    CLEAR(state) {
      state.token = ''
      state.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    login({ commit }, { username, password }) {
      return http.post('/users/login', { username, password }).then(res => {
        if (res.code === 0) {
          commit('SET_TOKEN', res.data.token)
          commit('SET_USER', res.data)
        }
        return res
      })
    },
    logout({ commit }) {
      commit('CLEAR')
    }
  }
})

export default store
