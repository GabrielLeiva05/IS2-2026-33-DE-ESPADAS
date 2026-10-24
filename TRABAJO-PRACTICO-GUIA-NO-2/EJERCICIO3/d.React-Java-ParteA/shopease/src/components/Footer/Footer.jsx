import React from 'react'
import FbIcon from '../common/FbIcon'
import InstaIcon from '../common/InstaIcon'

const Footer = ({ content }) => {
  return (
    <div className='bg-black text-white py-8'>
      <div className='flex flex-wrap justify-around gap-6 px-4'>
        {content?.items && content?.items?.map((item, index) => (
          <div className='flex flex-col' key={index}>
            <p className='text-[16px] pb-[10px]'>{item?.title}</p>
            {item?.list && item?.list?.map((listItem, i) => (
              <a key={i} className='flex flex-col text-[12px] py-2' href={listItem?.path}>
                {listItem?.label}
              </a>
            ))}
            {item?.description && <p>{item?.description}</p>}
          </div>
        ))}
      </div>
      <div className='flex gap-2 items-center justify-center py-4'>
        <a href='/fb'><FbIcon /></a>
        <a href='/insta'><InstaIcon /></a>
      </div>
      <p className='text-sm text-white text-center content-center'>{content?.copyright}</p>
    </div>
  )
}

export default Footer